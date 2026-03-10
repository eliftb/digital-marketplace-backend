package com.pazaryeri.service.impl;

import com.pazaryeri.dto.request.OrderRequest;
import com.pazaryeri.dto.response.OrderResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.entity.*;
import com.pazaryeri.enums.AccountStatus;
import com.pazaryeri.enums.DeliveryType;
import com.pazaryeri.enums.OrderStatus;
import com.pazaryeri.enums.UserRole;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.exception.ResourceNotFoundException;
import com.pazaryeri.repository.*;
import com.pazaryeri.service.EmailService;
import com.pazaryeri.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProducerProfileRepository producerProfileRepository;
    private final AddressRepository addressRepository;
    private final CommissionTransactionRepository commissionTransactionRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public OrderResponse createOrder(String consumerEmail, OrderRequest request) {
        User consumer = userRepository.findByEmail(consumerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Teslimat tipi SHIPPING ise adres zorunlu
        Address shippingAddress = null;
        if (request.getDeliveryType() == DeliveryType.SHIPPING) {
            if (request.getShippingAddressId() == null) {
                throw new BusinessException("Kargo teslimatı için adres seçimi zorunludur");
            }
            shippingAddress = addressRepository.findById(request.getShippingAddressId())
                    .filter(a -> a.getUser().getId().equals(consumer.getId()))
                    .orElseThrow(() -> new BusinessException("Geçersiz adres"));
        }

        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .consumer(consumer)
                .orderNumber(orderNumber)
                .deliveryType(request.getDeliveryType())
                .shippingAddress(shippingAddress)
                .notes(request.getNotes())
                .subtotal(BigDecimal.ZERO)
                .commissionTotal(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal commissionTotal = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ürün", itemRequest.getProductId()));

            if (!product.getActive()) {
                throw new BusinessException("Ürün aktif değil: " + product.getName());
            }

            ProducerProfile producer = product.getProducerProfile();
            if (producer.getApprovalStatus() != AccountStatus.ACTIVE) {
                throw new BusinessException("Üretici aktif değil: " + producer.getStoreName());
            }

            if (itemRequest.getQuantity() < product.getMinOrderQuantity()) {
                throw new BusinessException("Minimum sipariş miktarı " + product.getMinOrderQuantity() +
                        " olan ürün: " + product.getName());
            }

            // Stok kontrolü ve güncelleme
            product.decreaseStock(itemRequest.getQuantity());
            productRepository.save(product);

            BigDecimal itemSubtotal = product.getPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            BigDecimal commissionRate = producer.getCommissionRate();
            BigDecimal commissionAmount = itemSubtotal.multiply(commissionRate)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .producerProfile(producer)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .commissionRate(commissionRate)
                    .commissionAmount(commissionAmount)
                    .subtotal(itemSubtotal)
                    .build();

            order.getItems().add(orderItem);
            subtotal = subtotal.add(itemSubtotal);
            commissionTotal = commissionTotal.add(commissionAmount);
        }

        BigDecimal shippingFee = request.getDeliveryType() == DeliveryType.SHIPPING
                ? new BigDecimal("29.90") : BigDecimal.ZERO;

        order.setSubtotal(subtotal);
        order.setCommissionTotal(commissionTotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(subtotal.add(shippingFee));

        Order savedOrder = orderRepository.save(order);

        // Komisyon kayıtları oluştur
        for (OrderItem item : savedOrder.getItems()) {
            CommissionTransaction ct = CommissionTransaction.builder()
                    .orderItem(item)
                    .producerProfile(item.getProducerProfile())
                    .grossAmount(item.getSubtotal())
                    .commissionRate(item.getCommissionRate())
                    .commissionAmount(item.getCommissionAmount())
                    .netAmount(item.getSubtotal().subtract(item.getCommissionAmount()))
                    .build();
            commissionTransactionRepository.save(ct);
        }

        log.info("Sipariş oluşturuldu: {} - Tüketici: {}", orderNumber, consumerEmail);
        emailService.sendOrderConfirmationEmail(consumer.getEmail(), consumer.getFullName(), orderNumber);
        return toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı: " + orderNumber));

        // Admin her siparişi görebilir, tüketici sadece kendisini
        if (user.getRole() == UserRole.CONSUMER && !order.getConsumer().getId().equals(user.getId())) {
            throw new BusinessException("Bu siparişi görüntüleme yetkiniz yok");
        }

        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(String consumerEmail, Pageable pageable) {
        User consumer = userRepository.findByEmail(consumerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Page<Order> page = orderRepository.findByConsumerIdOrderByCreatedAtDesc(consumer.getId(), pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getProducerOrders(String producerEmail, Pageable pageable) {
        User user = userRepository.findByEmail(producerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        ProducerProfile producer = producerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Üretici profili bulunamadı"));

        Page<Order> page = orderRepository.findByProducerId(producer.getId(), pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = orderRepository.findAll(pageable);
        }
        return PageResponse.of(page.map(this::toResponse));
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş", orderId));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            // Üretici satış toplamını güncelle
            order.getItems().forEach(item -> {
                ProducerProfile producer = item.getProducerProfile();
                producer.setTotalSales(producer.getTotalSales().add(item.getSubtotal()));
                producerProfileRepository.save(producer);

                // Ürün satış sayısını güncelle
                Product product = item.getProduct();
                product.setSoldCount(product.getSoldCount() + item.getQuantity());
                productRepository.save(product);
            });
        }

        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusUpdateEmail(
            saved.getConsumer().getEmail(),
            saved.getConsumer().getFullName(),
            saved.getOrderNumber(),
            newStatus.name()
        );
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String consumerEmail) {
        User consumer = userRepository.findByEmail(consumerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş", orderId));

        if (!order.getConsumer().getId().equals(consumer.getId())) {
            throw new BusinessException("Bu siparişi iptal etme yetkiniz yok");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Sipariş bu aşamada iptal edilemez");
        }

        // Stokları iade et
        order.getItems().forEach(item -> {
            item.getProduct().increaseStock(item.getQuantity());
            productRepository.save(item.getProduct());
        });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.SHIPPED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED -> next == OrderStatus.REFUNDED;
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("Geçersiz durum geçişi: " + current + " -> " + next);
        }
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .status(item.getStatus())
                        .producerStoreName(item.getProducerProfile().getStoreName())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .deliveryType(order.getDeliveryType())
                .subtotal(order.getSubtotal())
                .commissionTotal(order.getCommissionTotal())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
