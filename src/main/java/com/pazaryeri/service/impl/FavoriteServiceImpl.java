package com.pazaryeri.service.impl;

import com.pazaryeri.dto.response.PageResponse;
import com.pazaryeri.dto.response.ProductResponse;
import com.pazaryeri.entity.Favorite;
import com.pazaryeri.entity.Product;
import com.pazaryeri.entity.User;
import com.pazaryeri.exception.BusinessException;
import com.pazaryeri.exception.ResourceNotFoundException;
import com.pazaryeri.repository.FavoriteRepository;
import com.pazaryeri.repository.ProductRepository;
import com.pazaryeri.repository.UserRepository;
import com.pazaryeri.service.FavoriteService;
import com.pazaryeri.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public void addFavorite(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Ürün", productId));

        if (favoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new BusinessException("Bu ürün zaten favorilerinizde");
        }

        favoriteRepository.save(Favorite.builder().user(user).product(product).build());
    }

    @Override
    @Transactional
    public void removeFavorite(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        favoriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(String userEmail, Long productId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        return favoriteRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getMyFavorites(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        return PageResponse.of(
                favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                        .map(fav -> productService.getProductById(fav.getProduct().getId()))
        );
    }
}
