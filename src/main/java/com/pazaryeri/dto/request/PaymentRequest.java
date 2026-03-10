package com.pazaryeri.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Sipariş ID zorunludur")
    private Long orderId;

    @NotBlank(message = "Ödeme yöntemi zorunludur")
    private String method; // CREDIT_CARD, BANK_TRANSFER

    // Kart bilgileri (gerçek sistemde ödeme sağlayıcısına gönderilir, burada saklanmaz)
    private String cardHolderName;
    private String cardNumber;  // Sadece son 4 hane saklanır
    private String expiryMonth;
    private String expiryYear;
    private String cvv;         // Asla saklanmaz
}
