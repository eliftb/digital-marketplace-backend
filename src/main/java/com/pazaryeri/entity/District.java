package com.pazaryeri.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "districts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class District {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    @Column(nullable = false) private String name;
}
