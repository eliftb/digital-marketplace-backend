package com.pazaryeri.dto.request;

import com.pazaryeri.enums.DeliveryType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank(message = "Ürün adı zorunludur")
    @Size(max = 300)
    private String name;

    @NotNull(message = "Kategori zorunludur")
    private Long categoryId;

    private String description;

    @NotNull(message = "Fiyat zorunludur")
    @DecimalMin(value = "0.01", message = "Fiyat 0'dan büyük olmalıdır")
    private BigDecimal price;

    @NotNull
    @Min(value = 0, message = "Stok negatif olamaz")
    private Integer stockQuantity;

    private String unit;

    @Min(value = 1)
    private Integer minOrderQuantity = 1;

    private DeliveryType deliveryType = DeliveryType.BOTH;

    private Boolean active = true;
    private Boolean featured = false;
    private List<String> imageUrls;
}
