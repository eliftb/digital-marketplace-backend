package com.pazaryeri.controller;

import com.pazaryeri.dto.request.PaymentRequest;
import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.PaymentResponse;
import com.pazaryeri.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ödemeler", description = "Ödeme yönetimi")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Ödeme yap")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Ödeme başarılı", response));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Siparişe ait ödeme bilgisi")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentByOrderId(orderId, userDetails.getUsername())));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Tüm ödemeler (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getAllPayments(PageRequest.of(page, size))));
    }

    @PostMapping("/admin/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Ödeme iade et (Admin)")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Ödeme iade edildi",
                paymentService.refundPayment(paymentId, userDetails.getUsername())));
    }
}
