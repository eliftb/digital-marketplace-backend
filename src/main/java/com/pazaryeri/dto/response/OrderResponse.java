package com.pazaryeri.dto.response;

import com.pazaryeri.enums.DeliveryType;
import com.pazaryeri.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private DeliveryType deliveryType;
    private BigDecimal subtotal;
    private BigDecimal commissionTotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String notes;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private OrderStatus status;
        private String producerStoreName;
    }
}
