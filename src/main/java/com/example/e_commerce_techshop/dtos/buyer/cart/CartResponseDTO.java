package com.example.e_commerce_techshop.dtos.buyer.cart;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {
    
    private String cartId;
    private String userId;
    private List<CartItemDTO> items;
    private Integer totalItems;
    private Long subtotal;
    private Long tax;
    private Long total;
    private String message;
}
