package com.example.e_commerce_techshop.services.chat;

import com.example.e_commerce_techshop.dtos.chat.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IChatService {

    // Conversation management
    ConversationDTO createConversation(CreateConversationRequest request, String currentUserId);
    
    ConversationDTO getConversation(String conversationId, String currentUserId);
    
    Page<ConversationDTO> getUserConversations(String userId, Pageable pageable);
    
    ConversationDTO getOrCreateConversation(String userId1, String userId2, String storeId);
    
    void archiveConversation(String conversationId, String currentUserId);
    
    Long getUnreadConversationsCount(String userId);
    
    // Message management
    ChatMessageDTO sendMessage(SendMessageRequest request, String senderId);
    
    Page<ChatMessageDTO> getConversationMessages(String conversationId, String currentUserId, Pageable pageable);
    
    void markMessageAsRead(String messageId, String userId);
    
    void markConversationAsRead(String conversationId, String userId);
    
    ChatMessageDTO deleteMessage(String messageId, String currentUserId);
}
