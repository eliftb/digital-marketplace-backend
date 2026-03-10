package com.pazaryeri.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class ReportResponse {

    @Data @Builder
    public static class SalesReport {
        private String period;
        private BigDecimal totalRevenue;
        private BigDecimal totalCommission;
        private long orderCount;
        private long productCount;
        private BigDecimal averageOrderValue;
    }

    @Data @Builder
    public static class ProducerReport {
        private Long producerId;
        private String storeName;
        private BigDecimal totalSales;
        private BigDecimal commissionPaid;
        private BigDecimal netEarnings;
        private long orderCount;
        private BigDecimal commissionRate;
    }

    @Data @Builder
    public static class TopProductReport {
        private Long productId;
        private String productName;
        private String storeName;
        private int soldCount;
        private BigDecimal totalRevenue;
        private BigDecimal rating;
    }

    @Data @Builder
    public static class CommissionReport {
        private String period;
        private BigDecimal totalCommission;
        private BigDecimal paidToProducers;
        private BigDecimal platformEarnings;
        private List<ProducerReport> byProducer;
    }
}
