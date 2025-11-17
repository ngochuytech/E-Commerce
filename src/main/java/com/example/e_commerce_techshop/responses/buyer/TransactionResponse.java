package com.example.e_commerce_techshop.responses.buyer;

import com.example.e_commerce_techshop.models.Transaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private String id;
    private String walletId;
    private String orderId;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
