package com.example.e_commerce_techshop.services.momo;

public interface IMomoService {
    /**
     * Tạo yêu cầu thanh toán MoMo
     * @param orderId Mã đơn hàng đã tạo trong hệ thống
     * @param amount Số tiền thanh toán (VND)
     * @return JSON response từ MoMo
     */
    String createPaymentRequest(String orderId, Long amount);

    /**
     * Kiểm tra trạng thái thanh toán
     * @param orderId Mã đơn hàng
     * @return JSON response từ MoMo
     */
    String checkPaymentStatus(String orderId);

    /**
     * Hoàn tiền giao dịch MoMo
     * @param transId Mã giao dịch MoMo (được trả về khi thanh toán thành công)
     * @param amount Số tiền hoàn (có thể hoàn một phần hoặc toàn bộ)
     * @param description Mô tả lý do hoàn tiền
     * @return JSON response từ MoMo
     */
    String refundPayment(Long transId, Long amount, String description);

    /**
     * Kiểm tra trạng thái hoàn tiền
     * @param orderId Mã đơn hàng của giao dịch hoàn tiền
     * @return JSON response từ MoMo
     */
    String checkRefundStatus(String orderId);
}
