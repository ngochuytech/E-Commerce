package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.AdminRevenue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdminRevenueResponse {
    private String id;
    private OrderResponse order;
    private StoreResponse store;
    private BigDecimal amount;
    private String revenueType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class OrderResponse {
        private String id;
        private BigDecimal totalPrice;
        private BigDecimal productPrice;
        private BigDecimal shippingFee;
        private BigDecimal serviceFee;
        private BigDecimal totalDiscountAmount;
        private String status;
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

    public static AdminRevenueResponse fromAdminRevenue(AdminRevenue adminRevenue) {
        OrderResponse orderResponse = null;
        StoreResponse storeResponse = null;
        if (adminRevenue.getOrder() != null) {
            orderResponse = OrderResponse.builder()
                    .id(adminRevenue.getOrder().getId())
                    .totalPrice(adminRevenue.getOrder().getTotalPrice())
                    .productPrice(adminRevenue.getOrder().getProductPrice())
                    .shippingFee(adminRevenue.getOrder().getShippingFee())
                    .serviceFee(adminRevenue.getOrder().getServiceFee())
                    .totalDiscountAmount(adminRevenue.getOrder().getTotalDiscountAmount())
                    .status(adminRevenue.getOrder().getStatus())
                    .build();
            if (adminRevenue.getOrder().getStore() != null) {
                storeResponse = StoreResponse.builder()
                        .id(adminRevenue.getOrder().getStore().getId())
                        .name(adminRevenue.getOrder().getStore().getName())
                        .logo(adminRevenue.getOrder().getStore().getLogoUrl())
                        .build();
            }
        }


        return AdminRevenueResponse.builder()
                .id(adminRevenue.getId())
                .order(orderResponse)
                .store(storeResponse)
                .amount(adminRevenue.getAmount())
                .revenueType(adminRevenue.getRevenueType())
                .description(adminRevenue.getDescription())
                .createdAt(adminRevenue.getCreatedAt())
                .updatedAt(adminRevenue.getUpdatedAt())
                .build();
    }
}
