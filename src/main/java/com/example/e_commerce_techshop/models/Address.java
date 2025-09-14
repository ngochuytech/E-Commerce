package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "province", length = 255, nullable = false)
    private String province;

    @Column(name = "district", length = 255, nullable = false)
    private String district;

    @Column(name = "ward", length = 255, nullable = false)
    private String ward;

    @Column(name = "home_address", length = 255, nullable = false)
    private String homeAddress;

    @Column(name = "suggested_name", length = 255)
    private String suggestedName;
}
