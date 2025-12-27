package com.example.e_commerce_techshop.dtos.admin;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for admin to decide on return quality dispute
 * When store disputes that returned goods have quality issues
 */
@Data
public class ReturnQualityDecisionDTO {
    
    /**
     * Decision: 
     * - APPROVE_STORE: Store thắng hoàn toàn - toàn bộ tiền cho store
     * - REJECT_STORE: Buyer thắng hoàn toàn - toàn bộ tiền hoàn cho buyer
     * - PARTIAL_REFUND: Store thắng nhưng buyer được hoàn một phần tiền
     */
    @NotBlank(message = "Decision is required")
    private String decision; // APPROVE_STORE, REJECT_STORE, or PARTIAL_REFUND
    
    /**
     * Admin's reason for the decision
     */
    @NotBlank(message = "Reason is required")
    private String reason;
    
    /**
     * Số tiền hoàn lại cho buyer (chỉ áp dụng khi decision = PARTIAL_REFUND)
     * Ví dụ: Đơn hàng 10 triệu, hàng thiếu phụ kiện 1 triệu -> partialRefundAmount = 9 triệu
     * Store sẽ nhận: totalAmount - partialRefundAmount = 1 triệu
     */
    private BigDecimal partialRefundAmount;
}
