package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "user_withdrawal_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWithdrawalRequest extends BaseEntity {
    
    @Id
    private String id;
    
    @DBRef
    private User user;
    
    @DBRef
    private UserWallet wallet;
    
    private BigDecimal amount; // Số tiền muốn rút
    
    private String bankName;
    
    private String bankAccountNumber;
    
    private String bankAccountName;
    
    private WithdrawalStatus status; // PENDING, APPROVED, REJECTED, COMPLETED
    
    private String note;
    
    private String adminNote;
    
    private String paymentGatewayTxnId; // Transaction ID từ Payment Gateway
    
    @DBRef
    private UserTransaction transaction; // Liên kết với giao dịch (sau khi hoàn thành)

    public enum WithdrawalStatus {
        PENDING,
        REJECTED,
        COMPLETED
    }
}
