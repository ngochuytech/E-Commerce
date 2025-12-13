package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model lưu các khiếu nại/tranh chấp giữa buyer và seller
 * Dùng làm chứng cứ cho admin xử lý
 */
@Document(collection = "disputes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Dispute extends BaseEntity {
    @Id
    private String id;

    @DBRef
    private ReturnRequest returnRequest;

    @DBRef
    private Order order;

    @DBRef
    private User buyer;

    @DBRef
    private Store store;

    // Loại tranh chấp
    private String disputeType;

    // Trạng thái tranh chấp
    private String status;

    // Danh sách các tin nhắn/comment trong tranh chấp
    private List<DisputeMessage> messages;

    // Admin xử lý
    @DBRef
    private User adminHandler;

    // Quyết định cuối cùng
    private String finalDecision;

    // Lý do quyết định
    private String decisionReason;

    // Thời gian giải quyết
    private LocalDateTime resolvedAt;

    // Bên thắng (BUYER hoặc STORE)
    private String winner;

    /**
     * Loại tranh chấp
     */
    public enum DisputeType {
        RETURN_REJECTION,       // Buyer khiếu nại store từ chối trả hàng
        RETURN_QUALITY          // Store khiếu nại hàng trả về có vấn đề
    }

    /**
     * Các trạng thái của tranh chấp
     */
    public enum DisputeStatus {
        OPEN,           // Mở tranh chấp
        IN_REVIEW,      // Admin đang xem xét
        RESOLVED,       // Đã giải quyết
        CLOSED          // Đã đóng
    }

    /**
     * Kết quả tranh chấp
     */
    public enum DisputeWinner {
        BUYER,          // Buyer thắng - được hoàn tiền/trả hàng
        STORE           // Store thắng - giữ nguyên quyết định từ chối
    }

    /**
     * Class lưu tin nhắn trong tranh chấp
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DisputeMessage {
        private String senderId;
        private String senderType; // BUYER, STORE, ADMIN
        private String senderName;
        private String content;
        private List<String> attachments; // URL hình ảnh/video đính kèm
        private LocalDateTime sentAt;
    }
}
