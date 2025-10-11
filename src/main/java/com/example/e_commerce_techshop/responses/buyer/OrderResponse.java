package com.example.e_commerce_techshop.responses.buyer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.Promotion;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String id;

    @JsonProperty("total_price")
    private BigDecimal totalPrice;

    @JsonProperty("discounted_price")
    private BigDecimal discountedPrice;

    @JsonProperty("payment_method")
    private String paymentMethod;

    private String status; // PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED

    @JsonProperty("order_items")
    private List<OrderItemResponse> orderItems;

    private UserResponse buyer;

    private StoreResponse store;

    @JsonProperty("address")
    private AddressResponse address;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
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
                .district(order.getAddress().getDistrict())
                .ward(order.getAddress().getWard())
                .homeAddress(order.getAddress().getHomeAddress())
                .suggestedName(order.getAddress().getSuggestedName())
                .build();

        BigDecimal discountedPrice = calculateDiscountedPrice(order);

        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .discountedPrice(discountedPrice)
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
    
    // Tính giá sau khi áp dụng promotion
    private static BigDecimal calculateDiscountedPrice(Order order) {
        BigDecimal originalPrice = order.getTotalPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        // 1. Kiểm tra có promotion không
        if (order.getPromotion() != null && order.getPromotion().getId() != null) {
            discountAmount = calculatePromotionDiscount(order, originalPrice);
        }
        
        // 2. Tính giá cuối cùng (không được âm)
        BigDecimal discountedPrice = originalPrice.subtract(discountAmount);
        return discountedPrice.max(BigDecimal.ZERO);
    }

    // Tính số tiền discount từ promotion
    private static BigDecimal calculatePromotionDiscount(Order order, BigDecimal originalPrice) {
        try {
            Promotion promotion = order.getPromotion();
            if (promotion == null) {
                return BigDecimal.ZERO;
            }
            
            // Kiểm tra minimum order value
            if (promotion.getMinOrderValue() != null && 
                originalPrice.compareTo(BigDecimal.valueOf(promotion.getMinOrderValue())) < 0) {
                return BigDecimal.ZERO;
            }
            
            BigDecimal discountAmount = BigDecimal.ZERO;
            
            // Tính discount theo type
            switch (promotion.getDiscountType().toUpperCase()) {
                case "PERCENTAGE":
                    // Discount theo %
                    discountAmount = originalPrice
                        .multiply(BigDecimal.valueOf(promotion.getDiscountValue()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    
                    // Kiểm tra max discount value
                    if (promotion.getMaxDiscountValue() != null && 
                        discountAmount.compareTo(BigDecimal.valueOf(promotion.getMaxDiscountValue())) > 0) {
                        discountAmount = BigDecimal.valueOf(promotion.getMaxDiscountValue());
                    }
                    break;
                    
                case "FIXED":
                    // Discount cố định
                    discountAmount = BigDecimal.valueOf(promotion.getDiscountValue());
                    break;
                    
                case "FREE_SHIPPING":
                    // Free shipping - không discount trên tổng tiền
                    discountAmount = BigDecimal.ZERO;
                    break;
                    
                default:
                    discountAmount = BigDecimal.ZERO;
                    break;
            }
            
            return discountAmount;
            
        } catch (Exception e) {
            // Log error và return 0 để tránh crash
            System.err.println("Error calculating promotion discount: " + e.getMessage());
            return BigDecimal.ZERO;
        }
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
}