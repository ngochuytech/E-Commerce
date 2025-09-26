package com.example.e_commerce_techshop.dtos.buyer.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    
    // Cart info
    private String cartId;
    private String userId;
    
    // Input fields for add/update
    @NotBlank(message = "Product variant ID không được để trống")
    private String productVariantId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    // Response fields
    private List<CartItem> items;
    private Integer totalItems;
    private Long subtotal;
    private Long tax;
    private Long total;
    private String message;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItem {
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
}
