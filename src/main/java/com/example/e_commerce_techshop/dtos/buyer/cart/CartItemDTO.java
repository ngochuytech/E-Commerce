package com.example.e_commerce_techshop.dtos.buyer.cart;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    
    private String cartItemId;
    private String productVariantId;
    private String productName;
    private String variantName;
    private Long price;
    private Integer quantity;
    private Long subtotal;
    private String imageUrl;
    private String storeName;
    private String storeId;
}
