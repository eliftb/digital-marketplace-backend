package com.pazaryeri.repository;

import com.pazaryeri.entity.Order;
import com.pazaryeri.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByConsumerIdOrderByCreatedAtDesc(Long consumerId, Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @Query("""
           SELECT o FROM Order o
           JOIN o.items oi
           WHERE oi.producerProfile.id = :producerId
           ORDER BY o.createdAt DESC
           """)
    Page<Order> findByProducerId(@Param("producerId") Long producerId, Pageable pageable);

    @Query("""
           SELECT o FROM Order o
           WHERE (:consumerId IS NULL OR o.consumer.id = :consumerId)
           AND (:status IS NULL OR o.status = :status)
           AND (:startDate IS NULL OR o.createdAt >= :startDate)
           AND (:endDate IS NULL OR o.createdAt <= :endDate)
           ORDER BY o.createdAt DESC
           """)
    Page<Order> filterOrders(@Param("consumerId") Long consumerId,
                             @Param("status") OrderStatus status,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate,
                             Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end")
    BigDecimal calculateRevenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
