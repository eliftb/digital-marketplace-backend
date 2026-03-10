package com.pazaryeri.repository;

import com.pazaryeri.entity.CommissionTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface CommissionTransactionRepository extends JpaRepository<CommissionTransaction, Long> {
    Page<CommissionTransaction> findByProducerProfileId(Long producerProfileId, Pageable pageable);
    Page<CommissionTransaction> findByPaidToProducerFalse(Pageable pageable);

    @Query("SELECT COALESCE(SUM(ct.commissionAmount), 0) FROM CommissionTransaction ct WHERE ct.createdAt BETWEEN :start AND :end")
    BigDecimal sumCommissionByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(ct.netAmount), 0) FROM CommissionTransaction ct WHERE ct.producerProfile.id = :producerId AND ct.paidToProducer = false")
    BigDecimal sumUnpaidNetAmountByProducer(@Param("producerId") Long producerId);
}
