package com.pazaryeri.dto.response;

import com.pazaryeri.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ProducerProfileResponse {
    private Long id;
    private Long userId;
    private String storeName;
    private String storeDescription;
    private String logoUrl;
    private String coverUrl;
    private String cityName;
    private String districtName;
    private String address;
    private BigDecimal commissionRate;
    private AccountStatus approvalStatus;
    private String rejectionReason;
    private BigDecimal totalSales;
    private BigDecimal rating;
    private Integer ratingCount;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
}
