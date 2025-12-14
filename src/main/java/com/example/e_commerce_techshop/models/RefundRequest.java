package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "refund_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefundRequest extends BaseEntity {
    @Id
    private String id;

    @DBRef
    private Order order;

    @DBRef
    private User buyer;

    private BigDecimal refundAmount; // Số tiền cần hoàn

    private String paymentMethod; // Phương thức thanh toán gốc (BANK_TRANSFER, E_WALLET)

    private String status; // PENDING, PROCESSING, COMPLETED, REJECTED

    private String refundTransactionId; // Mã giao dịch hoàn tiền

    private String adminNote; // Ghi chú từ admin khi xử lý

    private String rejectionReason; // Lý do từ chối (nếu có)

    @DBRef
    private User processedBy; // Admin xử lý

    public enum RefundStatus {
        PENDING,     // Chờ xử lý
        COMPLETED,   // Đã hoàn tiền
        REJECTED     // Từ chối
    }
}
