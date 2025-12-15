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

    private BigDecimal totalPrice; // Giá cuối cùng khách phải thanh toán

    private BigDecimal productPrice; // Giá sản phẩm (tổng)

    private BigDecimal shippingFee; // Phí ship

    private BigDecimal storeDiscountAmount; // Tiền giảm từ shop (shop chịu)

    private BigDecimal platformDiscountAmount; // Tiền giảm từ sàn (sàn chịu)

    private BigDecimal totalDiscountAmount; // Tổng tiền giảm giá

    private String paymentMethod;

    private String momoTransId; // Mã giao dịch MoMo (transId khi thanh toán thành công)

    private String paymentStatus; // Trạng thái thanh toán: UNPAID, PAID, FAILED

    private String status; // PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED

    private boolean isRated;

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
    private static class UserResponse {
        private String id;
        private String username;
        private String fullName;
        private String email;
        private String phone;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class StoreResponse {
        private String id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class AddressResponse {
        private String province;
        private String district;
        private String ward;
        private String homeAddress;
        private String suggestedName;
        private String phone;
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
                .fullName(order.getBuyer().getFullName())
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
                .phone(order.getAddress().getPhone())
                .build();

        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .productPrice(order.getProductPrice() != null ? order.getProductPrice() : BigDecimal.ZERO)
                .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO)
                .storeDiscountAmount(order.getStoreDiscountAmount() != null ? order.getStoreDiscountAmount() : BigDecimal.ZERO)
                .platformDiscountAmount(order.getPlatformDiscountAmount() != null ? order.getPlatformDiscountAmount() : BigDecimal.ZERO)
                .totalDiscountAmount(order.getTotalDiscountAmount() != null ? order.getTotalDiscountAmount() : BigDecimal.ZERO)
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .momoTransId(order.getMomoTransId())
                .status(order.getStatus())
                .isRated(order.isRated())
                .orderItems(orderItems)
                .buyer(buyer)
                .store(store)
                .address(address)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private String id;

        private String productVariantId;

        private String productName;

        private String productImage;

        private Integer quantity;

        private BigDecimal price;

        private String colorId;
    }
}