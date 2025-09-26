package com.example.e_commerce_techshop.dtos.buyer.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    
    // Identifiers
    private String id;
    private String buyerId;
    private String storeId;
    
    // Store info (for buyer)
    private String storeName;
    private String storeLogo;
    
    // Promotion & pricing
    private String promotionId;
    private BigDecimal totalPrice;
    
    // Address & payment
    private String addressId;
    private String shippingAddress;
    private String paymentMethod;
    
    // Status lifecycle
    private String status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Items
    private Integer itemCount;
    private List<OrderItem> orderItems;
    
    // Checkout input fields
    private String promotionIdInput; // For checkout input
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem {
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
}
