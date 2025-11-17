package com.example.e_commerce_techshop.services.wallet;

import com.example.e_commerce_techshop.models.Transaction;
import com.example.e_commerce_techshop.models.Wallet;
import com.example.e_commerce_techshop.models.WithdrawalRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface IWalletService {
    
    /**
     * Lấy thông tin ví của store
     */
    Wallet getStoreWallet(String storeId) throws Exception;
    
    /**
     * Lấy lịch sử giao dịch của store
     */
    Page<Transaction> getTransactionHistory(String storeId, int page, int size) throws Exception;
    
    /**
     * Tạo yêu cầu rút tiền
     */
    WithdrawalRequest createWithdrawalRequest(String storeId, BigDecimal amount, String bankName, 
                                              String bankAccountNumber, String bankAccountName, 
                                              String note) throws Exception;
    
    /**
     * Lấy danh sách yêu cầu rút tiền của store
     */
    Page<WithdrawalRequest> getWithdrawalRequests(String storeId, int page, int size) throws Exception;
    
    /**
     * Lấy chi tiết yêu cầu rút tiền
     */
    WithdrawalRequest getWithdrawalRequestDetail(String storeId, String requestId) throws Exception;
    
    /**
     * Admin: Lấy tất cả yêu cầu rút tiền (theo status)
     */
    Page<WithdrawalRequest> getAllWithdrawalRequests(String status, int page, int size) throws Exception;
    
    /**
     * Admin: Duyệt yêu cầu rút tiền
     */
    WithdrawalRequest approveWithdrawalRequest(String requestId, String adminNote) throws Exception;
    
    /**
     * Admin: Từ chối yêu cầu rút tiền
     */
    WithdrawalRequest rejectWithdrawalRequest(String requestId, String adminNote) throws Exception;
    
    /**
     * Admin: Hoàn thành chuyển tiền (sau khi đã chuyển tiền thực tế)
     */
    WithdrawalRequest completeWithdrawalRequest(String requestId) throws Exception;
}
