package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Store extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    private String description;

    private String logo_url;

    private String banner_url;

    @Column(name = "status", length = 50)
    private String status;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

}
