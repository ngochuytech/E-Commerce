package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.Transaction;

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
public class TransactionResponse {
    private String id;
    private WalletResponse wallet;
    private StoreResponse store;
    private OrderResponse order;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class WalletResponse {
        private String id;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class StoreResponse {
        private String id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class OrderResponse {
        private String id;
    }

    public static TransactionResponse fromTransaction(Transaction transaction) {
        WalletResponse walletResponse = WalletResponse.builder()
                .id(transaction.getWallet() != null ? transaction.getWallet().getId() : null)
                .build();

        StoreResponse storeResponse = null;
        if (transaction.getWallet() != null && transaction.getWallet().getStore() != null) {
            Store store = transaction.getWallet().getStore();
            storeResponse = StoreResponse.builder()
                    .id(store.getId())
                    .name(store.getName())
                    .logo(store.getLogoUrl())
                    .build();
        }

        OrderResponse orderResponse = null;
        if (transaction.getOrder() != null) {
            orderResponse = OrderResponse.builder()
                    .id(transaction.getOrder().getId())
                    .build();
        }
                
        return TransactionResponse.builder()
                .id(transaction.getId())
                .wallet(transaction.getWallet() != null ? walletResponse : null)
                .store(storeResponse)
                .order(orderResponse)
                .transactionType(transaction.getType() != null ? transaction.getType().toString() : null)
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
