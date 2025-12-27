package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseEntity {
    
    @Id
    private String id;
    
    @DBRef
    private Wallet wallet;
    
    @DBRef
    private Order order; // Liên kết với đơn hàng (nếu là giao dịch từ đơn hàng)
    
    private TransactionType type; // Loại giao dịch
    
    private BigDecimal amount; // Số tiền
    
    private BigDecimal balanceBefore; // Số dư trước giao dịch
    
    private BigDecimal balanceAfter; // Số dư sau giao dịch
    
    private String description; // Mô tả giao dịch
    
    private String status; // PENDING, COMPLETED, FAILED
    
    public enum TransactionType {
        ORDER_PENDING,   // Tiền chờ từ đơn hàng (chưa hoàn thành)
        ORDER_COMPLETED, // Tiền từ đơn hàng hoàn thành
        ORDER_CANCELLED, // Đơn hàng bị hủy - trừ pending
        WITHDRAWAL, // Rút tiền
        REFUND, // Hoàn tiền
        DISPUTE_LOSS, // Thua khiếu nại
        ADJUSTMENT // Điều chỉnh (admin)
    }
}
