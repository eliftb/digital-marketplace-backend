package com.pazaryeri.controller;

import com.pazaryeri.dto.request.AddressRequest;
import com.pazaryeri.dto.response.AddressResponse;
import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Adresler", description = "Kullanıcı adres yönetimi")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Adreslerim")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                addressService.getMyAddresses(userDetails.getUsername())));
    }

    @PostMapping
    @Operation(summary = "Yeni adres ekle")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Adres eklendi", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Adresi güncelle")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.updateAddress(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Adres güncellendi", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Adresi sil")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        addressService.deleteAddress(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successMessage("Adres silindi"));
    }

    @PatchMapping("/{id}/set-default")
    @Operation(summary = "Varsayılan adres yap")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AddressResponse response = addressService.setDefaultAddress(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Varsayılan adres güncellendi", response));
    }
}
