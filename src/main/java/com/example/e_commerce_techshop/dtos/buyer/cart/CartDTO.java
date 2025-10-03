package com.example.e_commerce_techshop.dtos.buyer.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    
    // Cart info
    @JsonProperty("user_id")
    private String userId;
    
    // Response fields
    private List<CartItemDTO> items;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItemDTO {
        // Input fields for add/update
        @JsonProperty("product_variant_id")
        @NotBlank(message = "Product variant ID không được để trống")
        private String productVariantId;
        
        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer quantity;
    }
}
