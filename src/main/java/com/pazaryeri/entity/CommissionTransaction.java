package com.pazaryeri.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "commission_transactions")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommissionTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_profile_id", nullable = false)
    private ProducerProfile producerProfile;

    @Column(name = "gross_amount", nullable = false) private BigDecimal grossAmount;
    @Column(name = "commission_rate", nullable = false) private BigDecimal commissionRate;
    @Column(name = "commission_amount", nullable = false) private BigDecimal commissionAmount;
    @Column(name = "net_amount", nullable = false) private BigDecimal netAmount;

    @Builder.Default @Column(name = "paid_to_producer") private Boolean paidToProducer = false;
    @Column(name = "paid_at") private LocalDateTime paidAt;

    @CreatedDate @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
