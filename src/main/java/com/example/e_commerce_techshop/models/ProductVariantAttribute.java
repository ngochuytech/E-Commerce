package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variant_attributes")
@IdClass(ProductVariantAttributeId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantAttribute {

    @Column(nullable = false)
    private String value;

    @Id
    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Id
    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;
}
