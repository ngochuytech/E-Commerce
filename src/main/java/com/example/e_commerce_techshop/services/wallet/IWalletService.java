package com.example.e_commerce_techshop.services.wallet;

import com.example.e_commerce_techshop.models.Transaction;
import com.example.e_commerce_techshop.models.Wallet;
import com.example.e_commerce_techshop.models.WithdrawalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IWalletService {
    
    /**
     * Lấy thông tin ví của store
     */
    Wallet getStoreWallet(String storeId) throws Exception;
    
    /**
     * Lấy lịch sử giao dịch của store
     */
    Page<Transaction> getTransactionHistory(String storeId, Pageable pageable) throws Exception;
    
    /**
     * Tạo yêu cầu rút tiền
     */
    WithdrawalRequest createWithdrawalRequest(String storeId, BigDecimal amount, String bankName, 
                                              String bankAccountNumber, String bankAccountName, 
                                              String note) throws Exception;
    
    /**
     * Lấy danh sách yêu cầu rút tiền của store
     */
    Page<WithdrawalRequest> getWithdrawalRequests(String storeId, Pageable pageable) throws Exception;
    
    /**
     * Lấy chi tiết yêu cầu rút tiền
     */
    WithdrawalRequest getWithdrawalRequestDetail(String storeId, String requestId) throws Exception;
    
    /**
     * Admin: Lấy tất cả yêu cầu rút tiền (theo status)
     */
    Page<WithdrawalRequest> getAllWithdrawalRequests(String status, Pageable pageable) throws Exception;
    
    /**
     * Admin: Từ chối yêu cầu rút tiền
     */
    WithdrawalRequest rejectWithdrawalRequest(String requestId, String adminNote) throws Exception;
    
    /**
     * Admin: Hoàn thành chuyển tiền (sau khi đã chuyển tiền thực tế)
     */
    WithdrawalRequest completeWithdrawalRequest(String requestId, String adminNote) throws Exception;
    
    /**
     * Cộng tiền vào ví shop khi đơn hàng được giao thành công
     * Số tiền = productPrice - storeDiscountAmount - serviceFee (không tính platform discount và phí ship)
     */
    void addOrderPaymentToWallet(String storeId, String orderId, BigDecimal amount, String description) throws Exception;

    /**
     * Hoàn tiền cho buyer khi trả hàng thành công
     * Tiền được hoàn vào ví user (UserWallet)
     */
    void refundToBuyer(String buyerId, String orderId, BigDecimal amount, String description) throws Exception;
}
