package com.example.e_commerce_techshop.dtos.admin;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessRefundRequestDTO {
    private String refundRequestId;
    private String action; // APPROVE, REJECT
    private String refundTransactionId; // Mã giao dịch chuyển khoản (nếu approve)
    private String adminNote; // Ghi chú
    private String rejectionReason; // Lý do từ chối (nếu reject)
}
