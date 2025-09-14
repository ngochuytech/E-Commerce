package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


enum ProductStatus {
    ACTIVE, HIDDEN, SOLD
}

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "category", nullable = false)
    private String category;

    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(name = "product_condition", nullable = false)
    private String productCondition;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    public static boolean getValidStatus(String status){
        try {
            ProductStatus statusEnum = ProductStatus.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
