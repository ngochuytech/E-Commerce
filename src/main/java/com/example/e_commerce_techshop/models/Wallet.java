package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "wallets")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet extends BaseEntity {
    
    @Id
    private String id;
    
    @DBRef
    private Store store;
    
    private BigDecimal balance; // Số dư hiện tại
    
    private BigDecimal totalEarned; // Tổng tiền đã kiếm được
    
    private BigDecimal totalWithdrawn; // Tổng tiền đã rút
    
    private BigDecimal pendingAmount; // Tiền đang chờ xử lý (đơn hàng chưa hoàn thành)
}
