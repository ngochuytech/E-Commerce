package com.example.e_commerce_techshop.dtos.b2c.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    
    private String id;
    private String buyerId;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String storeId;
    private String promotionId;
    private BigDecimal totalPrice;
    private String addressId;
    private String shippingAddress;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> orderItems;
    
    // Additional info for B2C
    private String notes;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
}
