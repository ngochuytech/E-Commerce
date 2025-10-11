package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CartItem extends BaseEntity {
    @Id
    private String id;

    @DBRef
    private Cart cart;

    @DBRef
    private ProductVariant productVariant;

    private int quantity;
    
    private Long unitPrice; // Store price at time of adding to cart
}