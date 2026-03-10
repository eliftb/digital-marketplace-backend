package com.pazaryeri.dto.response;

import com.pazaryeri.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private PaymentStatus status;
    private String method;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
