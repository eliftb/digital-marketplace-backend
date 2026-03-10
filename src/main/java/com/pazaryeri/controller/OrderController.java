package com.pazaryeri.controller;

import com.pazaryeri.dto.request.OrderRequest;
import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.dto.response.OrderResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.enums.OrderStatus;
import com.pazaryeri.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Siparişler", description = "Sipariş yönetimi")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CONSUMER')")
    @Operation(summary = "Sipariş ver")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sipariş oluşturuldu", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Benim siparişlerim")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getMyOrders(userDetails.getUsername(), pageable)));
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Sipariş detayı")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrderByNumber(orderNumber, userDetails.getUsername())));
    }

    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CONSUMER')")
    @Operation(summary = "Siparişi iptal et")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successMessage("Sipariş iptal edildi"));
    }

    // Üretici endpoint'leri
    @GetMapping("/producer")
    @PreAuthorize("hasRole('PRODUCER')")
    @Operation(summary = "Üretici siparişleri")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getProducerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getProducerOrders(userDetails.getUsername(), pageable)));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'PRODUCER')")
    @Operation(summary = "Sipariş durumunu güncelle")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Sipariş durumu güncellendi", response));
    }

    // Admin endpoint'leri
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Tüm siparişler (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(status, pageable)));
    }
}
