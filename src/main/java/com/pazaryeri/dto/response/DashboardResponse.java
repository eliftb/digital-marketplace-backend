package com.pazaryeri.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class DashboardResponse {
    // Kullanıcı istatistikleri
    private long totalUsers;
    private long totalProducers;
    private long totalConsumers;
    private long pendingProducerApprovals;

    // Ürün istatistikleri
    private long totalProducts;
    private long activeProducts;

    // Sipariş istatistikleri
    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;

    // Finansal istatistikler
    private BigDecimal totalRevenue;
    private BigDecimal totalCommissionEarned;
    private BigDecimal monthlyRevenue;
    private BigDecimal monthlyCommission;
}
