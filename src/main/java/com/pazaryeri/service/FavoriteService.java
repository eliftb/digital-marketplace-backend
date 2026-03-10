package com.pazaryeri.service;

import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {
    void addFavorite(String userEmail, Long productId);
    void removeFavorite(String userEmail, Long productId);
    boolean isFavorite(String userEmail, Long productId);
    PageResponse<ProductResponse> getMyFavorites(String userEmail, Pageable pageable);
}
