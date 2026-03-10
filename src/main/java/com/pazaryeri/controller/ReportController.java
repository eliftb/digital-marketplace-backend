package com.pazaryeri.controller;

import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ReportResponse;
import com.pazaryeri.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Raporlar", description = "Satış ve komisyon raporları")
public class ReportController {

    private final ReportService reportService;

    // ---- Admin Raporları ----
    @GetMapping("/admin/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Satış raporu (Admin)")
    public ResponseEntity<ApiResponse<ReportResponse.SalesReport>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSalesReport(startDate, endDate)));
    }

    @GetMapping("/admin/commission")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Komisyon raporu (Admin)")
    public ResponseEntity<ApiResponse<ReportResponse.CommissionReport>> getCommissionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getCommissionReport(startDate, endDate)));
    }

    @GetMapping("/admin/top-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "En çok satan ürünler (Admin)")
    public ResponseEntity<ApiResponse<List<ReportResponse.TopProductReport>>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getTopProducts(limit)));
    }

    @GetMapping("/admin/producers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Üretici bazlı rapor (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<ReportResponse.ProducerReport>>> getProducerReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getProducerReports(PageRequest.of(page, size))));
    }

    // ---- Üretici Kendi Raporu ----
    @GetMapping("/producer/my")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Kendi satış raporumu görüntüle (Üretici)")
    public ResponseEntity<ApiResponse<ReportResponse.ProducerReport>> getMyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reportService.getMyReport(userDetails.getUsername(), startDate, endDate)));
    }
}
