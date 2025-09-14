package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.User;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private String id;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastOrderDate;
    private Integer totalOrders;
    private BigDecimal totalSpent;
    private List<OrderResponse> recentOrders;

    public static CustomerResponse fromUser(User user) {
        return CustomerResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .avatarUrl(null) // TODO: Uncomment khi đã thêm avatarUrl vào User model
                .createdAt(user.getCreatedAt())
                .build();
    }
}
