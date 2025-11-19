package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class WalletResponse {
    private String id;
    private StoreResponse store;
    private BigDecimal balance;
    private BigDecimal totalEarned;
    private BigDecimal totalWithdrawn;
    private BigDecimal pendingAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class StoreResponse {
        private String id;
        private String name;
        private String logo;
        private String ownerName;
    }

    public static WalletResponse fromWallet(Wallet wallet) {
        StoreResponse storeResponse = StoreResponse.builder()
                .id(wallet.getStore() != null ? wallet.getStore().getId() : null)
                .name(wallet.getStore() != null ? wallet.getStore().getName() : null)
                .logo(wallet.getStore() != null ? wallet.getStore().getLogoUrl() : null)
                .ownerName(wallet.getStore() != null && wallet.getStore().getOwner() != null ? 
                    wallet.getStore().getOwner().getFullName() : "N/A")
                .build();

        return WalletResponse.builder()
                .id(wallet.getId())
                .store(storeResponse)
                .balance(wallet.getBalance())
                .totalEarned(wallet.getTotalEarned())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .pendingAmount(wallet.getPendingAmount())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }
}
