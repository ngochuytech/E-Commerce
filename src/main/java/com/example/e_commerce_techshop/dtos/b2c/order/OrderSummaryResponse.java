package com.example.e_commerce_techshop.dtos.b2c.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponse {
    
    private String id;
    private String buyerId;
    private String buyerName;
    private BigDecimal totalPrice;
    private String status;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional info for B2C
    private String paymentMethod;
    private String shippingAddress;
}
