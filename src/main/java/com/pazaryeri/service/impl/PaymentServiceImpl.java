package com.pazaryeri.service.impl;

import com.pazaryeri.dto.request.PaymentRequest;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.PaymentResponse;
import com.pazaryeri.entity.Order;
import com.pazaryeri.entity.Payment;
import com.pazaryeri.entity.User;
import com.pazaryeri.enums.OrderStatus;
import com.pazaryeri.enums.PaymentStatus;
import com.pazaryeri.enums.UserRole;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.exception.ResourceNotFoundException;
import com.pazaryeri.repository.OrderRepository;
import com.pazaryeri.repository.PaymentRepository;
import com.pazaryeri.repository.UserRepository;
import com.pazaryeri.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(String userEmail, PaymentRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş", request.getOrderId()));

        if (!order.getConsumer().getId().equals(user.getId())) {
            throw new BusinessException("Bu siparişe ödeme yapma yetkiniz yok");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Bu sipariş için ödeme yapılamaz. Durum: " + order.getStatus());
        }

        if (paymentRepository.findByOrderId(order.getId())
                .map(p -> p.getStatus() == PaymentStatus.COMPLETED).orElse(false)) {
            throw new BusinessException("Bu sipariş için ödeme zaten tamamlanmış");
        }

        // Gerçek ödeme sağlayıcısı entegrasyonu burada yapılır (İyzico, PayTR, vb.)
        // Şimdilik simüle ediyoruz
        String transactionId = simulatePayment(request);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(request.getMethod())
                .transactionId(transactionId)
                .status(PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Siparişi onayla
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Ödeme tamamlandı: Sipariş #{}, Tutar: {}", order.getOrderNumber(), order.getTotalAmount());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Ödeme bulunamadı"));

        // Admin her ödemeyi görebilir, tüketici sadece kendininkini
        if (user.getRole() == UserRole.CONSUMER &&
                !payment.getOrder().getConsumer().getId().equals(user.getId())) {
            throw new BusinessException("Bu ödemeyi görüntüleme yetkiniz yok");
        }

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getAllPayments(Pageable pageable) {
        return PageResponse.of(paymentRepository.findAll(pageable).map(this::toResponse));
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String adminEmail) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Ödeme", paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("Sadece tamamlanmış ödemeler iade edilebilir");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.getOrder().setStatus(OrderStatus.REFUNDED);
        orderRepository.save(payment.getOrder());

        log.info("Ödeme iade edildi: #{} - Admin: {}", paymentId, adminEmail);
        return toResponse(paymentRepository.save(payment));
    }

    /**
     * Gerçek ödeme sağlayıcısı entegrasyonu için placeholder.
     * Production'da İyzico, PayTR, Stripe gibi bir sağlayıcı kullanılır.
     */
    private String simulatePayment(PaymentRequest request) {
        // TODO: Gerçek ödeme sağlayıcısı entegre edilecek
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .orderNumber(p.getOrder().getOrderNumber())
                .amount(p.getAmount())
                .status(p.getStatus())
                .method(p.getMethod())
                .transactionId(p.getTransactionId())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
