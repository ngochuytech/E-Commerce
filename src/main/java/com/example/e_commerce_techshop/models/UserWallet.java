package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "user_wallets")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWallet extends BaseEntity {
    
    @Id
    private String id;
    
    @DBRef
    private User user;
    
    private BigDecimal balance; // Số dư hiện tại trong ví
    
    private BigDecimal totalRefunded; // Tổng tiền đã được hoàn lại từ đơn hàng hủy
    
    private BigDecimal totalSpent; // Tổng tiền đã chi tiêu từ ví
}
