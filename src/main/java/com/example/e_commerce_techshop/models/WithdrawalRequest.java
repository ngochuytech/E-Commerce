package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "withdrawal_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalRequest extends BaseEntity {
    
    @Id
    private String id;
    
    @DBRef
    private Store store;
    
    @DBRef
    private Wallet wallet;
    
    private BigDecimal amount; // Số tiền muốn rút
    
    private String bankName; // Tên ngân hàng
    
    private String bankAccountNumber; // Số tài khoản
    
    private String bankAccountName; // Tên chủ tài khoản
    
    private WithdrawalStatus status; 
    
    private String note; // Ghi chú của store
    
    private String adminNote; // Ghi chú của admin khi xử lý
    
    private String paymentGatewayTxnId; // Transaction ID từ Payment Gateway (VNPay, MoMo...)
    
    @DBRef
    private Transaction transaction; // Liên kết với giao dịch (sau khi hoàn thành)

    public enum WithdrawalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        COMPLETED
    }
}
