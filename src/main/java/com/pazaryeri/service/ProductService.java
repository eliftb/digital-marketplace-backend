package com.pazaryeri.service;

import com.pazaryeri.dto.request.ProductRequest;
import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ProductResponse;
import com.pazaryeri.enums.DeliveryType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    ProductResponse createProduct(String producerEmail, ProductRequest request);
    ProductResponse updateProduct(Long productId, String producerEmail, ProductRequest request);
    ProductResponse getProductById(Long id);
    ProductResponse getProductBySlug(String slug);
    PageResponse<ProductResponse> searchProducts(String search, Long categoryId, Long cityId,
                                                  DeliveryType deliveryType, BigDecimal minPrice,
                                                  BigDecimal maxPrice, Pageable pageable);
    PageResponse<ProductResponse> getProducerProducts(Long producerProfileId, Pageable pageable);
    PageResponse<ProductResponse> getFeaturedProducts(Pageable pageable);
    void deleteProduct(Long productId, String producerEmail);
    void toggleProductStatus(Long productId, String producerEmail);
}
