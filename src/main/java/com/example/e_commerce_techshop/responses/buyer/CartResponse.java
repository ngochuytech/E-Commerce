package com.example.e_commerce_techshop.responses.buyer;

import java.math.BigDecimal;
import java.util.List;

import com.example.e_commerce_techshop.models.Cart;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {

    private String id;
    
    private UserResponse user;
    
    private List<CartItemResponse> cartItems;

    private BigDecimal totalPrice;

    public static CartResponse fromCart(Cart cart) {
        UserResponse user = UserResponse.builder()
                .id(cart.getUser().getId())
                .fullName(cart.getUser().getFullName())
                .email(cart.getUser().getEmail())
                .phone(cart.getUser().getPhone())
                .build();

        List<CartItemResponse> cartItems = cart.getCartItems().stream()
            .<CartItemResponse>map(cartItem -> CartItemResponse.builder()
                    .productId(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getId() : null)
                    .productName(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getName() : null)
                    .imageUrl(cartItem.getProductVariant() != null ? cartItem.getProductVariant().getPrimaryImageUrl() : null)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProductVariant() != null ? BigDecimal.valueOf(cartItem.getProductVariant().getPrice()) : null)
                    .colorId(cartItem.getColorId())
                    .colorName(cartItem.getProductVariant() != null && cartItem.getProductVariant().getColors() != null 
                        ? cartItem.getProductVariant().getColors().stream()
                            .filter(color -> color.getId().equals(cartItem.getColorId()))
                            .findFirst()
                            .<String>map(color -> color.getColorName())
                            .orElse(null)
                        : null
                    )
                    .build()
            ).collect(java.util.stream.Collectors.toList());

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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class UserResponse {
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
class CartItemResponse {

    private String productId;

    private String productName;

    private String imageUrl;

    private int quantity;

    private BigDecimal price;
    
    private String colorId;

    private String colorName;
}

