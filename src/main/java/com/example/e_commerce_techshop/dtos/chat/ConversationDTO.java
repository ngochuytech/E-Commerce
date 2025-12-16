package com.example.e_commerce_techshop.dtos.chat;

import com.example.e_commerce_techshop.models.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    
    private String id;
    private List<ParticipantInfo> participants;
    private Conversation.ConversationType type;
    
    private String storeId;
    private String storeName;
    private String storeAvatar;
    
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    
    private String productId;
    private String productName;
    
    private Integer unreadCount;
    private Conversation.ConversationStatus status;
    
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParticipantInfo {
        private String userId;
        private String userName;
        private String avatar;
        private Boolean online;
    }
}
