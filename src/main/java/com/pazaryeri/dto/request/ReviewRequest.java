package com.pazaryeri.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "Ürün ID zorunludur")
    private Long productId;

    private Long orderItemId;

    @NotNull(message = "Puan zorunludur")
    @Min(value = 1, message = "Puan en az 1 olmalıdır")
    @Max(value = 5, message = "Puan en fazla 5 olabilir")
    private Short rating;

    @Size(max = 1000, message = "Yorum en fazla 1000 karakter olabilir")
    private String comment;
}
