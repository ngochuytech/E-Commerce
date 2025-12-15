package com.example.e_commerce_techshop.models.Static;

import java.math.BigDecimal;
import java.util.List;

import com.example.e_commerce_techshop.models.Promotion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class chứa thông tin tài chính của đơn hàng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFinancials {
    private BigDecimal storeTotal;
    private BigDecimal storeDiscountAmount;
    private BigDecimal platformDiscountAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal finalShippingFee;
    private BigDecimal platformCommission; // Hoa hồng sàn 5%
    private BigDecimal finalTotal;
    private List<Promotion> appliedPromotions;
    private boolean isPlatformOrderPromotionApplied;

}
