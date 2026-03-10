package com.pazaryeri.dto.request;

import com.pazaryeri.enums.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull(message = "Teslimat tipi zorunludur")
    private DeliveryType deliveryType;

    private Long shippingAddressId;

    @NotEmpty(message = "Sipariş kalemleri boş olamaz")
    @Valid
    private List<OrderItemRequest> items;

    private String notes;

    @Data
    public static class OrderItemRequest {
        @NotNull private Long productId;

        @NotNull
        @Min(value = 1, message = "Miktar en az 1 olmalıdır")
        private Integer quantity;
    }
}
