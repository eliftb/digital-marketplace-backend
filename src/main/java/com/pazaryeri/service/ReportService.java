package com.pazaryeri.service;

import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ReportResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    ReportResponse.SalesReport getSalesReport(LocalDate startDate, LocalDate endDate);
    ReportResponse.CommissionReport getCommissionReport(LocalDate startDate, LocalDate endDate);
    List<ReportResponse.TopProductReport> getTopProducts(int limit);
    PageResponse<ReportResponse.ProducerReport> getProducerReports(Pageable pageable);
    ReportResponse.ProducerReport getMyReport(String producerEmail, LocalDate startDate, LocalDate endDate);
}
