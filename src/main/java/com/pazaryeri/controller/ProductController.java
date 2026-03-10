package com.pazaryeri.controller;

import com.pazaryeri.dto.request.ProductRequest;
import com.pazaryeri.dto.response.ApiResponse;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ProductResponse;
import com.pazaryeri.enums.DeliveryType;
import com.pazaryeri.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Ürünler", description = "Ürün yönetimi ve arama")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Ürün ara ve filtrele (herkese açık)")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) DeliveryType deliveryType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        PageResponse<ProductResponse> response = productService.searchProducts(
                search, categoryId, cityId, deliveryType, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/featured")
    @Operation(summary = "Öne çıkan ürünler")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.getFeaturedProducts(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ürün detayı (ID ile)")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Ürün detayı (Slug ile)")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }

    @GetMapping("/producer/{producerProfileId}")
    @Operation(summary = "Üreticinin ürünleri")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducerProducts(
            @PathVariable Long producerProfileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProducerProducts(producerProfileId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ürün ekle (Üretici)")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Ürün eklendi", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ürün güncelle (Üretici)")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Ürün güncellendi", response));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('PRODUCER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ürün aktif/pasif durumunu değiştir")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.toggleProductStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successMessage("Ürün durumu değiştirildi"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ürün sil (pasif yap)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.deleteProduct(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successMessage("Ürün silindi"));
    }
}
