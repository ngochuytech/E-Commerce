package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "user_transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTransaction extends BaseEntity {
    
    @Id
    private String id;
    
    @DBRef
    private UserWallet wallet;
    
    @DBRef
    private Order order; // Liên kết với đơn hàng (nếu là giao dịch từ đơn hàng)
    
    private TransactionType type; // Loại giao dịch
    
    private BigDecimal amount; // Số tiền
    
    private BigDecimal balanceBefore; // Số dư trước giao dịch
    
    private BigDecimal balanceAfter; // Số dư sau giao dịch
    
    private String description; // Mô tả giao dịch

    private String withDrawalId;
    
    public enum TransactionType {
        REFUND, // Hoàn tiền từ đơn hàng bị hủy
        PAYMENT, // Thanh toán đơn hàng bằng ví
        WITHDRAWAL, // Rút tiền về tài khoản ngân hàng
        ADJUSTMENT // Điều chỉnh số dư (admin)
    }
}
