package com.pazaryeri.controller;

import com.pazaryeri.dto.request.ProducerProfileRequest;
import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ProducerProfileResponse;
import com.pazaryeri.enums.AccountStatus;
import com.pazaryeri.service.ProducerService;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/producers")
@RequiredArgsConstructor
@Tag(name = "Üreticiler", description = "Üretici profili yönetimi")
public class ProducerController {

    private final ProducerService producerService;

    // === Public endpoints ===
    @GetMapping("/public")
    @Operation(summary = "Üreticileri ara (herkese açık)")
    public ResponseEntity<ApiResponse<PageResponse<ProducerProfileResponse>>> searchProducers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                producerService.searchProducers(search, cityId, AccountStatus.ACTIVE, pageable)));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Üretici detayı (herkese açık)")
    public ResponseEntity<ApiResponse<ProducerProfileResponse>> getProducerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(producerService.getProducerById(id)));
    }

    // === Kimlik doğrulama gerektiren endpoints ===
    @PostMapping("/register")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Üretici olarak kaydol")
    public ResponseEntity<ApiResponse<ProducerProfileResponse>> registerAsProducer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProducerProfileRequest request) {
        ProducerProfileResponse response = producerService.registerAsProducer(
                userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Üretici başvurusu alındı, onay bekleniyor", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PRODUCER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Kendi üretici profilim")
    public ResponseEntity<ApiResponse<ProducerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                producerService.getMyProfile(userDetails.getUsername())));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PRODUCER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Üretici profilimi güncelle")
    public ResponseEntity<ApiResponse<ProducerProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProducerProfileRequest request) {
        ProducerProfileResponse response = producerService.updateProfile(
                userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Profil güncellendi", response));
    }

    // === Admin endpoints ===
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tüm üreticiler (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<ProducerProfileResponse>>> getAllProducers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                producerService.searchProducers(search, cityId, status, pageable)));
    }

    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Üreticiyi onayla")
    public ResponseEntity<ApiResponse<ProducerProfileResponse>> approveProducer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProducerProfileResponse response = producerService.approveProducer(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Üretici onaylandı", response));
    }

    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Üreticiyi reddet")
    public ResponseEntity<ApiResponse<ProducerProfileResponse>> rejectProducer(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProducerProfileResponse response = producerService.rejectProducer(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Üretici reddedildi", response));
    }

    @PatchMapping("/admin/{id}/commission")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Üretici komisyon oranını güncelle (Süper Admin)")
    public ResponseEntity<ApiResponse<Void>> updateCommissionRate(
            @PathVariable Long id,
            @RequestParam BigDecimal rate,
            @AuthenticationPrincipal UserDetails userDetails) {
        producerService.updateCommissionRate(id, rate, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successMessage("Komisyon oranı güncellendi"));
    }
}
