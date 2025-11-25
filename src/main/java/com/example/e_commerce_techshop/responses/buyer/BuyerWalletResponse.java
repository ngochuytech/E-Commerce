package com.example.e_commerce_techshop.responses.buyer;

import java.math.BigDecimal;

import com.example.e_commerce_techshop.models.UserWallet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyerWalletResponse {
    private String id;
    private UserResponse user;
    private BigDecimal balance;
    private BigDecimal totalRefunded;
    private BigDecimal totalSpent;

    @Builder
    @Data
    private static class UserResponse {
        private String id;
        private String userName;
    }

    public static BuyerWalletResponse fromUserWallet(UserWallet wallet) {
        if (wallet == null) {
            return null;
        }
        return BuyerWalletResponse.builder()
                .id(wallet.getId())
                .user(UserResponse.builder()
                        .id(wallet.getUser().getId())
                        .userName(wallet.getUser().getUsername())
                        .build())
                .balance(wallet.getBalance())
                .totalRefunded(wallet.getTotalRefunded())
                .totalSpent(wallet.getTotalSpent())
                .build();
    }
}
