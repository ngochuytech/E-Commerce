package com.example.e_commerce_techshop.dtos.momo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoRefundRequest {
    /**
     * Mã giao dịch MoMo (transId từ response khi thanh toán thành công)
     */
    private Long transId;
    
    /**
     * Số tiền hoàn (Min: 1,000 VND, Max: 50,000,000 VND)
     * Hoàn một phần: amount < số tiền đã thanh toán
     * Hoàn toàn bộ: amount = số tiền đã thanh toán
     */
    private Long amount;
    
    /**
     * Mô tả lý do hoàn tiền
     */
    private String description;
}
