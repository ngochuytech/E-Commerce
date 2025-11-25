package com.example.e_commerce_techshop.responses.admin;

import com.example.e_commerce_techshop.models.WithdrawalRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminWithdrawalResponse {
    private String id;                      // Request ID
    private StoreResponse store;
    private BigDecimal amount;              // Withdrawal amount
    private String bankName;                // Bank name
    private String bankAccountNumber;       // Bank account number
    private String bankAccountName;         // Bank account name
    private String status;                  // PENDING / REJECTED / COMPLETED
    private String note;                    // Store's note
    private String adminNote;               // Admin's note (reason for reject or transfer note)
    private String transactionId;           // Transaction ID (only when COMPLETED)
    private LocalDateTime createdAt;        // Request creation date
    private LocalDateTime updatedAt;        // Last update date

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
        private String ownerEmail;
    }


    public static AdminWithdrawalResponse fromWithdrawalRequest(WithdrawalRequest request) {
        StoreResponse storeResponse = StoreResponse.builder()
                .id(request.getStore().getId())
                .name(request.getStore().getName())
                .logo(request.getStore().getLogoUrl())
                .ownerName(request.getStore().getOwner() != null ? request.getStore().getOwner().getFullName() : "N/A")
                .ownerEmail(request.getStore().getOwner() != null ? request.getStore().getOwner().getEmail() : "N/A")
                .build();   

        return AdminWithdrawalResponse.builder()
                .id(request.getId())
                .store(storeResponse)
                .amount(request.getAmount())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankAccountName(request.getBankAccountName())
                .status(request.getStatus().name())
                .note(request.getNote())
                .adminNote(request.getAdminNote())
                .transactionId(request.getTransaction() != null ? request.getTransaction().getId() : null)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
