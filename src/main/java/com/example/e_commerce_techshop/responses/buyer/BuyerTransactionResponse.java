package com.example.e_commerce_techshop.responses.buyer;

import java.math.BigDecimal;

import com.example.e_commerce_techshop.models.UserTransaction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BuyerTransactionResponse {
    private String id;
    private OrderResponse order;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String createdAt;
    private String updatedAt;

    @Data
    @Builder
    private static class OrderResponse{
        private String id;
        private String name;
    }

    public static BuyerTransactionResponse fromTransaction(UserTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        OrderResponse orderResponse = null;
        if (transaction.getOrder()!=null) {
            orderResponse = OrderResponse.builder()
                    .id(transaction.getOrder().getId())
                    .name(transaction.getOrder().getId())
                    .build();
        }
        return BuyerTransactionResponse.builder()
                .id(transaction.getId())
                .order(orderResponse)
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null)
                .updatedAt(transaction.getUpdatedAt() != null ? transaction.getUpdatedAt().toString() : null)
                .build();
    }

}
