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
    private AddressResponse address;
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
                .buyerId(order.getBuyer().getId())
                .storeId(order.getStore().getId())
                .promotionId(order.getPromotion().getId())
                .totalPrice(order.getTotalPrice())
                .address(order.getAddress() != null ? AddressResponse.builder()
                        .province(order.getAddress().getProvince())
                        .ward(order.getAddress().getWard())
                        .homeAddress(order.getAddress().getHomeAddress())
                        .suggestedName(order.getAddress().getSuggestedName())
                        .build() : null)
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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class AddressResponse {
    private String id;
    private String province;
    private String district;
    private String ward;
    private String homeAddress;
    private String suggestedName;
}




