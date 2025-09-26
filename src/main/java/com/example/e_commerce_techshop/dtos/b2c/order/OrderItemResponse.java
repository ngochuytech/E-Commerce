package com.example.e_commerce_techshop.dtos.b2c.order;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    
    private String id;
    private String productVariantId;
    private String productName;
    private String variantName;
    private String productImage;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    
    // Additional info for B2C
    private String productId;
    private String category;
    private String brand;
    private Integer stock;
}
