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
        List<OrderItemResponse> orderItems = null;
        if (order.getOrderItems() != null) {
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> OrderItemResponse.builder()
                            .id(orderItem.getId())
                            .productVariantId(orderItem.getProductVariant().getId())
                            .quantity(orderItem.getQuantity())
                            .price(BigDecimal.valueOf(orderItem.getPrice()))
                            .productName(orderItem.getProductVariant().getProduct().getName())
                            .variantName(orderItem.getProductVariant().getName())
                            .colorId(orderItem.getColorId())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyer().getId())
                .storeId(order.getStore().getId())
                .promotionId(order.getPromotion() != null ? order.getPromotion().getId() : null)
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
                .orderItems(orderItems)
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
    private String colorId;
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




