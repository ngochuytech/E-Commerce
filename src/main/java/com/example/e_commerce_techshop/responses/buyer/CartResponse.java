package com.example.e_commerce_techshop.responses.buyer;

import java.math.BigDecimal;
import java.util.List;

import com.example.e_commerce_techshop.models.Cart;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {

    private String id;
    
    private UserResponse user;
    
    @JsonProperty("cart_items")
    private List<CartItemResponse> cartItems;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class UserResponse {
        private String id;
        private String fullName;
        private String email;
        private String phone;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class CartItemResponse {
        private String id;

        @JsonProperty("product_id")
        private String productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("image_url")
        private String imageUrl;

        private int quantity;

        private BigDecimal price;
    }

    public static CartResponse fromCart(Cart cart) {
        UserResponse user = UserResponse.builder()
                .id(cart.getUser().getId())
                .fullName(cart.getUser().getFullName())
                .email(cart.getUser().getEmail())
                .phone(cart.getUser().getPhone())
                .build();

        List<CartItemResponse> cartItems = cart.getCartItems().stream()
            .map(cartItem -> CartItemResponse.builder()
                        .id(cartItem.getId())
                        .productId(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getProduct().getId() : null)
                        .productName(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getName() : null)
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getProductVariant() != null ? BigDecimal.valueOf(cartItem.getProductVariant().getPrice()) : null)
                        .build()
            ).toList();

        BigDecimal totalPrice = cartItems.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .user(user)
                .cartItems(cartItems)
                .totalPrice(totalPrice)
                .build();
    }
    
}
