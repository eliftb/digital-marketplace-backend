package com.pazaryeri.service.impl;

import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ReportResponse;
import com.pazaryeri.entity.ProducerProfile;
import com.pazaryeri.entity.User;
import com.pazaryeri.enums.OrderStatus;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.exception.ResourceNotFoundException;
import com.pazaryeri.repository.*;
import com.pazaryeri.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProducerProfileRepository producerProfileRepository;
    private final CommissionTransactionRepository commissionTransactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportResponse.SalesReport getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        BigDecimal totalRevenue = orderRepository.calculateRevenueByDateRange(start, end);
        BigDecimal totalCommission = commissionTransactionRepository.sumCommissionByDateRange(start, end);
        long orderCount = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long activeProducts = productRepository.countByActiveTrue();

        BigDecimal avgOrderValue = (orderCount > 0 && totalRevenue.compareTo(BigDecimal.ZERO) > 0)
                ? totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ReportResponse.SalesReport.builder()
                .period(startDate + " - " + endDate)
                .totalRevenue(totalRevenue)
                .totalCommission(totalCommission)
                .orderCount(orderCount)
                .productCount(activeProducts)
                .averageOrderValue(avgOrderValue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse.CommissionReport getCommissionReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        BigDecimal totalCommission = commissionTransactionRepository.sumCommissionByDateRange(start, end);

        List<ReportResponse.ProducerReport> producerReports = producerProfileRepository
                .findAll()
                .stream()
                .map(pp -> {
                    BigDecimal unpaid = commissionTransactionRepository
                            .sumUnpaidNetAmountByProducer(pp.getId());
                    return ReportResponse.ProducerReport.builder()
                            .producerId(pp.getId())
                            .storeName(pp.getStoreName())
                            .totalSales(pp.getTotalSales())
                            .commissionRate(pp.getCommissionRate())
                            .netEarnings(unpaid)
                            .build();
                })
                .collect(Collectors.toList());

        return ReportResponse.CommissionReport.builder()
                .period(startDate + " - " + endDate)
                .totalCommission(totalCommission)
                .byProducer(producerReports)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse.TopProductReport> getTopProducts(int limit) {
        return productRepository.findAll()
                .stream()
                .filter(p -> p.getActive() && p.getSoldCount() > 0)
                .sorted((a, b) -> b.getSoldCount() - a.getSoldCount())
                .limit(limit)
                .map(p -> ReportResponse.TopProductReport.builder()
                        .productId(p.getId())
                        .productName(p.getName())
                        .storeName(p.getProducerProfile().getStoreName())
                        .soldCount(p.getSoldCount())
                        .totalRevenue(p.getPrice().multiply(BigDecimal.valueOf(p.getSoldCount())))
                        .rating(p.getRating())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse.ProducerReport> getProducerReports(Pageable pageable) {
        Page<ProducerProfile> page = producerProfileRepository.findAll(pageable);
        return PageResponse.of(page.map(pp -> ReportResponse.ProducerReport.builder()
                .producerId(pp.getId())
                .storeName(pp.getStoreName())
                .totalSales(pp.getTotalSales())
                .commissionRate(pp.getCommissionRate())
                .netEarnings(commissionTransactionRepository.sumUnpaidNetAmountByProducer(pp.getId()))
                .build()));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse.ProducerReport getMyReport(String producerEmail,
                                                      LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(producerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        ProducerProfile pp = producerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Üretici profili bulunamadı"));

        BigDecimal unpaidNet = commissionTransactionRepository.sumUnpaidNetAmountByProducer(pp.getId());

        return ReportResponse.ProducerReport.builder()
                .producerId(pp.getId())
                .storeName(pp.getStoreName())
                .totalSales(pp.getTotalSales())
                .commissionRate(pp.getCommissionRate())
                .netEarnings(unpaidNet)
                .build();
    }
}
