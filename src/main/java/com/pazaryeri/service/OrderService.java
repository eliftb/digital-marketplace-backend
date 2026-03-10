package com.pazaryeri.service;

import com.pazaryeri.dto.request.OrderRequest;
import com.pazaryeri.dto.response.OrderResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(String consumerEmail, OrderRequest request);
    OrderResponse getOrderByNumber(String orderNumber, String userEmail);
    PageResponse<OrderResponse> getMyOrders(String consumerEmail, Pageable pageable);
    PageResponse<OrderResponse> getProducerOrders(String producerEmail, Pageable pageable);
    PageResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus, String userEmail);
    void cancelOrder(Long orderId, String consumerEmail);
}
