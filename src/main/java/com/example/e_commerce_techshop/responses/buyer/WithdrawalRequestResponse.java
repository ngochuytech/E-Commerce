package com.example.e_commerce_techshop.responses.buyer;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalRequestResponse {
    private String id;
    private String storeId;
    private String storeName;
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String status;
    private String note;
    private String adminNote;
    private String transactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
