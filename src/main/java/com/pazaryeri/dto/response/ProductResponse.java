package com.pazaryeri.dto.response;

import com.pazaryeri.enums.DeliveryType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String unit;
    private Integer minOrderQuantity;
    private DeliveryType deliveryType;
    private Boolean active;
    private Boolean featured;
    private Integer soldCount;
    private BigDecimal rating;
    private Integer ratingCount;
    private List<String> imageUrls;
    private CategoryResponse category;
    private ProducerSummaryResponse producer;
    private LocalDateTime createdAt;

    @Data @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String slug;
    }

    @Data @Builder
    public static class ProducerSummaryResponse {
        private Long id;
        private String storeName;
        private String logoUrl;
        private String cityName;
        private BigDecimal rating;
    }
}
