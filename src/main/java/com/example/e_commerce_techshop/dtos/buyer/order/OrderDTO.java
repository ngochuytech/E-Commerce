package com.example.e_commerce_techshop.dtos.buyer.order;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    
    // Địa chỉ giao hàng (bắt buộc)
    @JsonProperty("address_id")
    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String addressId;

    // Phương thức thanh toán (bắt buộc)
    @JsonProperty("payment_method")
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // COD, VNPAY, MOMO, etc.
    
    // Mã giảm giá (tùy chọn)
    @JsonProperty("promotion_code")
    private String promotionCode;
    
    // Ghi chú đơn hàng (tùy chọn)
    private String note;
}
