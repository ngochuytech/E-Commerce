package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chat_messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {
    @Id
    private String id;

    private String conversationId;

    private String senderId;
    private String senderName;
    private String senderAvatar;

    private String content;

    private MessageType type;

    // For image/file messages
    private List<String> attachments;

    // Read status by each participant
    private List<String> readByUserIds;

    // Reply to another message
    private String replyToMessageId;

    // Message status
    private MessageStatus status;

    private LocalDateTime sentAt;

    public enum MessageType {
        TEXT, // Tin nhắn text thông thường
        IMAGE, // Tin nhắn có ảnh
        FILE, // Tin nhắn có file đính kèm
        SYSTEM, // Thông báo hệ thống: "User đã tham gia cuộc trò chuyệ"
        PRODUCT_LINK // Chia sẻ link sản phẩm trong chat
    }

    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ,
        DELETED
    }
}
