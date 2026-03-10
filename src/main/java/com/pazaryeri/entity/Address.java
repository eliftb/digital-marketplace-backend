package com.pazaryeri.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity @Table(name = "addresses")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false) private String title;
    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(nullable = false) private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(columnDefinition = "TEXT", nullable = false) private String address;
    @Column(name = "zip_code") private String zipCode;

    @Builder.Default
    @Column(name = "is_default") private Boolean isDefault = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
}
