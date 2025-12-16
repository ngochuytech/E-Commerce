package com.example.e_commerce_techshop.services.userWallet;

import com.example.e_commerce_techshop.models.UserWallet;
import com.example.e_commerce_techshop.models.UserTransaction;
import com.example.e_commerce_techshop.models.UserWithdrawalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface IUserWalletService {

    /**
     * Lấy hoặc tạo ví cho khách hàng
     */
    UserWallet getOrCreateUserWallet(String userId) throws Exception;

    /**
     * Hoàn tiền vào ví khách hàng (khi hủy đơn hàng đã thanh toán)
     */
    UserTransaction refundToWallet(String userId, String orderId, BigDecimal amount, String description) throws Exception;

    /**
     * Tạo yêu cầu rút tiền từ ví (chờ admin duyệt)
     */
    UserWithdrawalRequest createWithdrawalRequest(String userId, BigDecimal amount, String bankName, 
                                                   String bankAccountNumber, String bankAccountName, String note) throws Exception;

    /**
     * Lấy danh sách yêu cầu rút tiền của khách hàng
     */
    Page<UserWithdrawalRequest> getWithdrawalRequests(String userId, Pageable pageable) throws Exception;

    /**
     * Lấy chi tiết yêu cầu rút tiền
     */
    UserWithdrawalRequest getWithdrawalRequestDetail(String userId, String requestId) throws Exception;

    /**
     * Lấy tất cả yêu cầu rút tiền (admin)
     */
    Page<UserWithdrawalRequest> getAllWithdrawalRequests(String status, Pageable pageable) throws Exception;

    /**
     * Admin từ chối yêu cầu rút tiền
     */
    UserWithdrawalRequest rejectWithdrawalRequest(String requestId, String adminNote) throws Exception;

    /**
     * Admin hoàn thành yêu cầu rút tiền
     */
    UserWithdrawalRequest completeWithdrawalRequest(String requestId, String adminNote) throws Exception;

    /**
     * Lấy thông tin ví của khách hàng
     */
    UserWallet getWalletInfo(String userId) throws Exception;

    /**
     * Lấy lịch sử giao dịch
     */
    Page<UserTransaction> getTransactionHistory(String userId, Pageable pageable) throws Exception;

    /**
     * Lấy số dư ví
     */
    BigDecimal getWalletBalance(String userId) throws Exception;

    /**
     * Sử dụng tiền từ ví để thanh toán (khi thanh toán bằng ví)
     */
    UserTransaction paymentFromWallet(String userId, String orderId, BigDecimal amount) throws Exception;

    /**
     * Điều chỉnh số dư ví (admin)
     */
    UserTransaction adjustWalletBalance(String userId, BigDecimal amount, String description) throws Exception;
}
