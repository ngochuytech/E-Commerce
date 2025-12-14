package com.example.e_commerce_techshop.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoPaymentRequest {
    /**
     * Mã đơn hàng đã tạo trong hệ thống
     */
    private String orderId;
    
    /**
     * Số tiền thanh toán (VND)
     * Min: 1,000 VND
     * Max: 50,000,000 VND
     */
    private Long amount;
}
