package com.example.e_commerce_techshop.services.refund;

import java.math.BigDecimal;

import com.example.e_commerce_techshop.models.Order;

public interface IRefundService {
    /**
     * Tạo yêu cầu hoàn tiền cho đơn hàng
     */
    void createRefundRequest(Order order, BigDecimal refundAmount) throws Exception;

    /**
     * Xử lý hoàn tiền qua MoMo
     * @param orderId Mã đơn hàng
     * @param amount Số tiền hoàn
     * @param momoTransId Mã giao dịch MoMo gốc
     * @return Mã giao dịch hoàn tiền
     */
    String processMomoRefund(String orderId, BigDecimal amount, String momoTransId, String description) throws Exception;

    /**
     * Kiểm tra trạng thái hoàn tiền
     */
    String checkRefundStatus(String orderId) throws Exception;
}
