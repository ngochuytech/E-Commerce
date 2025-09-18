package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Order;
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
    private String storeId;
    private String promotionId;
    private BigDecimal totalPrice;
    private String addressId;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> orderItems;
    
    // Additional info
    private String buyerName;
    private String storeName;
    private String addressDetails;

    public static OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .storeId(order.getStoreId())
                .promotionId(order.getPromotionId())
                .totalPrice(order.getTotalPrice())
                .addressId(order.getAddressId())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class OrderItemResponse {
    private String id;
    private String productVariantId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String variantName;
}



