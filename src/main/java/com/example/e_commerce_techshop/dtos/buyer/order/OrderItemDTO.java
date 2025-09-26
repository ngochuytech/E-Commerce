package com.example.e_commerce_techshop.dtos.buyer.order;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemDTO {
    
    private String id;
    
    private String productVariantId;
    private String productName;
    private String productImage;
    
    private String storeName;
    private String storeLogo;
    
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
