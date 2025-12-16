package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    // Get messages by conversation with pagination
    Page<ChatMessage> findByConversationIdOrderBySentAtDesc(String conversationId, Pageable pageable);

    // Get messages after a specific time
    @Query("{ 'conversationId': ?0, 'sentAt': { $gt: ?1 } }")
    List<ChatMessage> findByConversationIdAndSentAtAfter(String conversationId, LocalDateTime after);

    // Count unread messages in conversation for a user
    @Query(value = "{ 'conversationId': ?0, 'readByUserIds': { $nin: [?1] }, 'senderId': { $ne: ?1 } }", count = true)
    Long countUnreadMessages(String conversationId, String userId);

    // Find messages by sender
    Page<ChatMessage> findBySenderIdOrderBySentAtDesc(String senderId, Pageable pageable);

    // Delete messages in conversation (soft delete by updating status)
    @Query("{ 'conversationId': ?0 }")
    List<ChatMessage> findByConversationId(String conversationId);
}
