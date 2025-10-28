package com.example.e_commerce_techshop.responses.buyer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.e_commerce_techshop.models.Order;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String id;

    private BigDecimal totalPrice;

    private String paymentMethod;

    private String status; // PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED

    private List<OrderItemResponse> orderItems;

    private UserResponse buyer;

    private StoreResponse store;

    private AddressResponse address;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class UserResponse {
        private String id;
        private String username;
        private String email;
        private String phone;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class StoreResponse {
        private String id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class AddressResponse {
        private String province;
        private String district;
        private String ward;
        private String homeAddress;
        private String suggestedName;
    }

    public static OrderResponse fromOrder(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(orderItem -> {
                    String primaryImageUrl = orderItem.getProductVariant().getPrimaryImageUrl();
                    
                    return OrderItemResponse.builder()
                            .id(orderItem.getId())
                            .productVariantId(orderItem.getProductVariant().getId())
                            .productName(orderItem.getProductVariant().getProduct().getName())
                            .productImage(primaryImageUrl)
                            .quantity(orderItem.getQuantity())
                            .price(BigDecimal.valueOf(orderItem.getPrice()))
                            .colorId(orderItem.getColorId())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        UserResponse buyer = UserResponse.builder()
                .id(order.getBuyer().getId())
                .username(order.getBuyer().getUsername())
                .email(order.getBuyer().getEmail())
                .phone(order.getBuyer().getPhone())
                .build();
    
        StoreResponse store = StoreResponse.builder()
                .id(order.getStore().getId())
                .name(order.getStore().getName())
                .logo(order.getStore().getLogoUrl())
                .build();

        AddressResponse address = AddressResponse.builder()
                .province(order.getAddress().getProvince())
                .ward(order.getAddress().getWard())
                .homeAddress(order.getAddress().getHomeAddress())
                .suggestedName(order.getAddress().getSuggestedName())
                .build();

        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .orderItems(orderItems)
                .buyer(buyer)
                .store(store)
                .address(address)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class OrderItemResponse {
    private String id;

    private String productVariantId;

    private String productName;

    private String productImage;

    private Integer quantity;

    private BigDecimal price;
    
    private String colorId;
}