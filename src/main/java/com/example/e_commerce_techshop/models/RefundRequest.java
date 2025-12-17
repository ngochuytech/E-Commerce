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

    // ========== BANK ACCOUNT FOR REFUND ==========
    
    // Tên ngân hàng (cho hoàn tiền)
    private String bankName;
    
    // Số tài khoản ngân hàng (cho hoàn tiền)
    private String bankAccountNumber;
    
    // Tên chủ tài khoản (cho hoàn tiền)
    private String bankAccountName;

    public enum PaymentMethod {
        BANK_TRANSFER, // Hoàn tiền qua chuyển khoản ngân hàng
        E_WALLET      // Hoàn tiền qua ví điện tử (MoMo, ZaloPay, ...)
    }

    public enum RefundStatus {
        PENDING,     // Chờ xử lý
        COMPLETED,   // Đã hoàn tiền
        REJECTED     // Từ chối
    }
}
