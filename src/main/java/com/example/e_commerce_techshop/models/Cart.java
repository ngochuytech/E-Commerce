package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart extends BaseEntity {
    
    @Id
    private String id;
    
    @DBRef
    private User user;
    
    @Builder.Default
    private List<CartItemEmbedded> cartItems = new ArrayList<>();
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CartItemEmbedded {
        @DBRef
        private ProductVariant productVariant;
        private int quantity;
        private Long unitPrice;
        private String colorId;
    }
}