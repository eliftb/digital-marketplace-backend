package com.pazaryeri.repository;

import com.pazaryeri.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductIdAndApprovedTrue(Long productId, Pageable pageable);
    Page<Review> findByConsumerId(Long consumerId, Pageable pageable);
    Page<Review> findByApprovedFalse(Pageable pageable);
    boolean existsByOrderItemIdAndConsumerId(Long orderItemId, Long consumerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Double calculateAverageRating(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    long countApprovedByProductId(@Param("productId") Long productId);
}
