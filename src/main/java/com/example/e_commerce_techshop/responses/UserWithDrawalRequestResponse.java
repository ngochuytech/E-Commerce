package com.example.e_commerce_techshop.responses;

import java.math.BigDecimal;

import com.example.e_commerce_techshop.models.UserWithdrawalRequest.WithdrawalStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserWithDrawalRequestResponse {
    private String id;

    private UserResponse user;

    private BigDecimal amount;

    private String bankName;

    private String bankAccountNumber;

    private String bankAccountName;

    private WithdrawalStatus status; 

    private String note;

    private String adminNote;

    private String paymentGatewayTxnId;

    @Data
    @Builder
    private static class UserResponse {
        private String id;
        private String fullName;
        private String email;
    }

    public static UserWithDrawalRequestResponse fromUserWithdrawalRequest(com.example.e_commerce_techshop.models.UserWithdrawalRequest request) {
        UserResponse userResponse = UserResponse.builder()
                .id(request.getUser().getId())
                .fullName(request.getUser().getFullName())
                .email(request.getUser().getEmail())
                .build();

        return UserWithDrawalRequestResponse.builder()
                .id(request.getId())
                .user(userResponse)
                .amount(request.getAmount())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankAccountName(request.getBankAccountName())
                .status(request.getStatus())
                .note(request.getNote())
                .adminNote(request.getAdminNote())
                .paymentGatewayTxnId(request.getPaymentGatewayTxnId())
                .build();
    }
}
