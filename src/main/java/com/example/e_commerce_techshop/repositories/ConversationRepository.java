package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    // Find conversations by participant
    @Query("{ 'participantIds': ?0, 'status': { $ne: 'CLOSED' } }")
    Page<Conversation> findByParticipantId(String userId, Pageable pageable);

    // Find conversation between specific users
    @Query("{ 'participantIds': { $all: ?0 }, 'type': ?1 }")
    Optional<Conversation> findByParticipantIdsAndType(List<String> participantIds, Conversation.ConversationType type);

    // Find conversations by store
    @Query("{ 'storeId': ?0, 'status': { $ne: 'CLOSED' } }")
    Page<Conversation> findByStoreId(String storeId, Pageable pageable);

    // Find conversation between buyer and store
    @Query("{ 'participantIds': { $all: ?0 }, 'storeId': ?1, 'type': 'BUYER_SELLER' }")
    Optional<Conversation> findBuyerStoreConversation(List<String> participantIds, String storeId);

    // Count unread conversations for user
    @Query(value = "{ 'participantIds': ?0, 'unreadCounts': { $elemMatch: { 'userId': ?0, 'count': { $gt: 0 } } } }", count = true)
    Long countUnreadConversations(String userId);
}
