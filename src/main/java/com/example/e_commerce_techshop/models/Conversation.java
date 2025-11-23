package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "conversations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversation extends BaseEntity {
    @Id
    private String id;

    // List of participant user IDs
    private List<String> participantIds;

    // Type: BUYER_SELLER, BUYER_SUPPORT, etc.
    private ConversationType type;

    // Store ID if conversation is between buyer and seller
    private String storeId;

    // Last message info for quick display
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    // For BUYER_SELLER: product context
    private String productId;
    private String productName;

    // Unread count per participant
    private List<UnreadCount> unreadCounts;

    // Conversation status
    private ConversationStatus status;

    public enum ConversationType {
        BUYER_SELLER,      // Chat between buyer and store
        BUYER_SUPPORT,     // Chat between buyer and admin support
        SELLER_SUPPORT     // Chat between seller and admin support
    }

    public enum ConversationStatus {
        ACTIVE,
        ARCHIVED,
        CLOSED
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnreadCount {
        private String userId;
        private Integer count;
    }
}
