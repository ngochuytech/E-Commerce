package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.AdminRevenue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdminRevenueResponse {
    private String id;
    private String orderId;
    private BigDecimal serviceFee;
    private String revenueType;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminRevenueResponse fromAdminRevenue(AdminRevenue adminRevenue) {
        return AdminRevenueResponse.builder()
                .id(adminRevenue.getId())
                .orderId(adminRevenue.getOrder() != null ? adminRevenue.getOrder().getId() : null)
                .serviceFee(adminRevenue.getServiceFee())
                .revenueType(adminRevenue.getRevenueType())
                .status(adminRevenue.getStatus())
                .description(adminRevenue.getDescription())
                .createdAt(adminRevenue.getCreatedAt())
                .updatedAt(adminRevenue.getUpdatedAt())
                .build();
    }
}
