package com.example.e_commerce_techshop.services.chat;

import com.example.e_commerce_techshop.dtos.chat.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IChatService {

    /**  Tạo cuộc trò chuyện mới  */
    ConversationDTO createConversation(CreateConversationRequest request, String currentUserId);
    
    /** Lấy thông tin cuộc trò chuyện */
    ConversationDTO getConversation(String conversationId, String currentUserId);
    
    /** Lấy danh sách cuộc trò chuyện của user */
    Page<ConversationDTO> getUserConversations(String userId, Pageable pageable);
    
    /** Lấy hoặc tạo cuộc trò chuyện giữa 2 user trong cửa hàng */
    ConversationDTO getOrCreateConversation(String userId1, String userId2, String storeId);
    
    /** Lưu trữ cuộc trò chuyện */
    void archiveConversation(String conversationId, String currentUserId);
    
    /** Lấy số lượng cuộc trò chuyện chưa đọc */
    Long getUnreadConversationsCount(String userId);
    
    /** Quản lý tin nhắn */
    ChatMessageDTO sendMessage(SendMessageRequest request, String senderId);
    
    /** Lấy tin nhắn trong cuộc trò chuyện */
    Page<ChatMessageDTO> getConversationMessages(String conversationId, String currentUserId, Pageable pageable);
    
    /** Đánh dấu tin nhắn đã đọc */
    void markMessageAsRead(String messageId, String userId);
    
    /** Đánh dấu toàn bộ cuộc trò chuyện đã đọc */
    void markConversationAsRead(String conversationId, String userId);
    
    /** Xóa tin nhắn */
    ChatMessageDTO deleteMessage(String messageId, String currentUserId);
}
