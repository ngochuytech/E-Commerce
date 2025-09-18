package com.example.e_commerce_techshop.dtos.b2c.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @NotNull(message = "ID người mua không được để trống")
    private String buyerId;

    @NotNull(message = "ID cửa hàng không được để trống")
    private String storeId;

    private String promotionId;

    @NotNull(message = "Tổng giá không được để trống")
    @Positive(message = "Tổng giá phải lớn hơn 0")
    private BigDecimal totalPrice;

    @NotNull(message = "ID địa chỉ không được để trống")
    private String addressId;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}



