package com.pazaryeri.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ReviewResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long consumerId;
    private String consumerName;
    private Short rating;
    private String comment;
    private Boolean approved;
    private LocalDateTime createdAt;
}
