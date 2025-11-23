package com.example.e_commerce_techshop.dtos.chat;

import com.example.e_commerce_techshop.models.ChatMessage;
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
public class ChatMessageDTO {
    
    private String id;
    private String conversationId;
    
    private String senderId;
    private String senderName;
    private String senderAvatar;
    
    private String content;
    private ChatMessage.MessageType type;
    private List<String> attachments;
    
    private List<String> readByUserIds;
    private String replyToMessageId;
    
    private ChatMessage.MessageStatus status;
    private LocalDateTime sentAt;
    
    // For product link messages
    private ProductInfo productInfo;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductInfo {
        private String productId;
        private String productName;
        private String imageUrl;
        private Double price;
    }
}
