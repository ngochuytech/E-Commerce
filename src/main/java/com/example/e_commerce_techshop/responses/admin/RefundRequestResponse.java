package com.example.e_commerce_techshop.responses.admin;

import com.example.e_commerce_techshop.models.RefundRequest;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundRequestResponse {
    private String id;
    private String orderId;
    private String orderStatus;
    private BigDecimal refundAmount;
    private String paymentMethod;
    
    // Thông tin khách hàng
    private String buyerId;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    
    // Thông tin tài khoản ngân hàng
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountName;
    
    private String status;
    private String refundTransactionId;
    private String adminNote;
    private String rejectionReason;
    
    // Thông tin admin xử lý
    private String processedById;
    private String processedByName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RefundRequestResponse fromRefundRequest(RefundRequest request) {
        RefundRequestResponseBuilder builder = RefundRequestResponse.builder()
                .id(request.getId())
                .orderId(request.getOrder() != null ? request.getOrder().getId() : null)
                .orderStatus(request.getOrder() != null ? request.getOrder().getStatus() : null)
                .refundAmount(request.getRefundAmount())
                .paymentMethod(request.getPaymentMethod())
                .buyerId(request.getBuyer() != null ? request.getBuyer().getId() : null)
                .buyerName(request.getBuyer() != null ? request.getBuyer().getFullName() : null)
                .buyerEmail(request.getBuyer() != null ? request.getBuyer().getEmail() : null)
                .buyerPhone(request.getBuyer() != null ? request.getBuyer().getPhone() : null)
                .bankName(request.getBankName())
                .accountNumber(request.getBankAccountNumber())
                .accountName(request.getBankAccountName())
                .status(request.getStatus())
                .refundTransactionId(request.getRefundTransactionId())
                .adminNote(request.getAdminNote())
                .rejectionReason(request.getRejectionReason())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt());
        
        // Thông tin admin xử lý
        if (request.getProcessedBy() != null) {
            builder.processedById(request.getProcessedBy().getId())
                   .processedByName(request.getProcessedBy().getFullName());
        }
        
        return builder.build();
    }
}
