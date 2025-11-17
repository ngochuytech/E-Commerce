package com.example.e_commerce_techshop.responses.buyer;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletResponse {
    private String id;
    private String storeId;
    private String storeName;
    private BigDecimal balance;
    private BigDecimal totalEarned;
    private BigDecimal totalWithdrawn;
    private BigDecimal pendingAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
