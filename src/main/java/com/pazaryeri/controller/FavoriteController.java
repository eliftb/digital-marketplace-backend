package com.pazaryeri.controller;

import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ProductResponse;
import com.pazaryeri.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Favoriler", description = "Favori ürün yönetimi")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Favori ürünlerim")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getMyFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                favoriteService.getMyFavorites(userDetails.getUsername(), PageRequest.of(page, size))));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Favoriye ekle")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favoriteService.addFavorite(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.successMessage("Favorilere eklendi"));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Favoriden çıkar")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favoriteService.removeFavorite(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.successMessage("Favorilerden çıkarıldı"));
    }

    @GetMapping("/{productId}/check")
    @Operation(summary = "Ürün favoride mi?")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isFav = favoriteService.isFavorite(userDetails.getUsername(), productId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("isFavorite", isFav)));
    }
}
