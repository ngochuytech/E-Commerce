package com.example.e_commerce_techshop.services.chat;

import com.example.e_commerce_techshop.dtos.chat.*;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.*;
import com.example.e_commerce_techshop.repositories.ChatMessageRepository;
import com.example.e_commerce_techshop.repositories.ConversationRepository;
import com.example.e_commerce_techshop.repositories.user.UserRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ConversationDTO createConversation(CreateConversationRequest request, String currentUserId) {
        // Validate current user exists
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        String recipientId;
        
        // For BUYER_SELLER: get seller from store
        if (request.getType() == Conversation.ConversationType.BUYER_SELLER) {
            if (request.getStoreId() == null) {
                throw new IllegalArgumentException("Store ID is required for buyer-seller conversation");
            }
            
            Store store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new DataNotFoundException("Store not found"));
            
            // Get store owner as recipient
            recipientId = store.getOwner().getId();
        } else {
            // For other types, use provided recipientId
            if (request.getRecipientId() == null) {
                throw new IllegalArgumentException("Recipient ID is required");
            }
            recipientId = request.getRecipientId();
        }

        // Validate recipient exists
        userRepository.findById(recipientId)
                .orElseThrow(() -> new DataNotFoundException("Recipient not found"));

        List<String> participantIds = Arrays.asList(currentUserId, recipientId);

        // Check if conversation already exists
        var existingConv = conversationRepository.findByParticipantIdsAndType(
                participantIds, request.getType());

        if (existingConv.isPresent()) {
            return convertToDTO(existingConv.get(), currentUserId);
        }

        // Get product info if provided
        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId()).orElse(null);
        }

        // Create new conversation
        List<Conversation.UnreadCount> unreadCounts = participantIds.stream()
                .map(id -> new Conversation.UnreadCount(id, 0))
                .collect(Collectors.toList());

        Conversation conversation = Conversation.builder()
                .participantIds(participantIds)
                .type(request.getType())
                .storeId(request.getStoreId())
                .productId(request.getProductId())
                .productName(product != null ? product.getName() : null)
                .unreadCounts(unreadCounts)
                .status(Conversation.ConversationStatus.ACTIVE)
                .build();

        conversation = conversationRepository.save(conversation);

        // Send initial message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().isEmpty()) {
            SendMessageRequest messageRequest = SendMessageRequest.builder()
                    .conversationId(conversation.getId())
                    .content(request.getInitialMessage())
                    .type(ChatMessage.MessageType.TEXT)
                    .build();
            sendMessage(messageRequest, currentUserId);
        }

        return convertToDTO(conversation, currentUserId);
    }

    @Override
    public ConversationDTO getConversation(String conversationId, String currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new DataNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUserId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        return convertToDTO(conversation, currentUserId);
    }

    @Override
    public Page<ConversationDTO> getUserConversations(String userId, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findByParticipantId(userId, pageable);
        return conversations.map(conv -> convertToDTO(conv, userId));
    }

    @Override
    @Transactional
    public ConversationDTO getOrCreateConversation(String userId1, String userId2, String storeId) {
        List<String> participantIds = Arrays.asList(userId1, userId2);

        var existingConv = conversationRepository.findBuyerStoreConversation(participantIds, storeId);

        if (existingConv.isPresent()) {
            return convertToDTO(existingConv.get(), userId1);
        }

        // Create new conversation
        CreateConversationRequest request = CreateConversationRequest.builder()
                .recipientId(userId2)
                .type(Conversation.ConversationType.BUYER_SELLER)
                .storeId(storeId)
                .build();

        return createConversation(request, userId1);
    }

    @Override
    @Transactional
    public void archiveConversation(String conversationId, String currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new DataNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUserId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        conversation.setStatus(Conversation.ConversationStatus.ARCHIVED);
        conversationRepository.save(conversation);
    }

    @Override
    public Long getUnreadConversationsCount(String userId) {
        return conversationRepository.countUnreadConversations(userId);
    }

    @Override
    @Transactional
    public ChatMessageDTO sendMessage(SendMessageRequest request, String senderId) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new DataNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(senderId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        // Create message
        ChatMessage message = ChatMessage.builder()
                .conversationId(request.getConversationId())
                .senderId(senderId)
                .senderName(sender.getFullName())
                .senderAvatar(sender.getAvatar())
                .content(request.getContent())
                .type(request.getType())
                .attachments(request.getAttachments())
                .replyToMessageId(request.getReplyToMessageId())
                .readByUserIds(new ArrayList<>(List.of(senderId)))
                .status(ChatMessage.MessageStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        message = chatMessageRepository.save(message);

        // Update conversation
        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageTime(message.getSentAt());

        // Update unread counts for other participants
        List<Conversation.UnreadCount> updatedCounts = conversation.getUnreadCounts().stream()
                .map(count -> {
                    if (!count.getUserId().equals(senderId)) {
                        count.setCount(count.getCount() + 1);
                    }
                    return count;
                })
                .collect(Collectors.toList());
        conversation.setUnreadCounts(updatedCounts);

        conversationRepository.save(conversation);

        // Send via WebSocket to all participants
        ChatMessageDTO messageDTO = convertMessageToDTO(message);
        
        // Broadcast to all participants via user queue
        conversation.getParticipantIds().forEach(participantId -> {
            messagingTemplate.convertAndSendToUser(
                    participantId,
                    "/queue/messages",
                    messageDTO
            );
        });
        
        // Also broadcast to conversation topic for redundancy
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId(),
                messageDTO
        );

        return messageDTO;
    }

    @Override
    public Page<ChatMessageDTO> getConversationMessages(String conversationId, String currentUserId, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new DataNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(currentUserId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        Page<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderBySentAtDesc(
                conversationId, pageable);

        return messages.map(this::convertMessageToDTO);
    }

    @Override
    @Transactional
    public void markMessageAsRead(String messageId, String userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new DataNotFoundException("Message not found"));

        if (!message.getReadByUserIds().contains(userId)) {
            message.getReadByUserIds().add(userId);
            message.setStatus(ChatMessage.MessageStatus.READ);
            chatMessageRepository.save(message);

            // Send read receipt via WebSocket
            MessageReadReceiptRequest receipt = MessageReadReceiptRequest.builder()
                    .conversationId(message.getConversationId())
                    .messageId(messageId)
                    .userId(userId)
                    .build();

            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + message.getConversationId() + "/read",
                    receipt
            );
        }
    }

    @Override
    @Transactional
    public void markConversationAsRead(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new DataNotFoundException("Conversation not found"));

        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        // Reset unread count for this user
        List<Conversation.UnreadCount> updatedCounts = conversation.getUnreadCounts().stream()
                .map(count -> {
                    if (count.getUserId().equals(userId)) {
                        count.setCount(0);
                    }
                    return count;
                })
                .collect(Collectors.toList());
        conversation.setUnreadCounts(updatedCounts);
        conversationRepository.save(conversation);

        // Mark all unread messages as read
        List<ChatMessage> unreadMessages = chatMessageRepository.findByConversationId(conversationId).stream()
                .filter(msg -> !msg.getReadByUserIds().contains(userId))
                .collect(Collectors.toList());

        unreadMessages.forEach(msg -> {
            msg.getReadByUserIds().add(userId);
            msg.setStatus(ChatMessage.MessageStatus.READ);
        });

        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(unreadMessages);
        }
    }

    @Override
    @Transactional
    public ChatMessageDTO deleteMessage(String messageId, String currentUserId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new DataNotFoundException("Message not found"));

        if (!message.getSenderId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own messages");
        }

        message.setStatus(ChatMessage.MessageStatus.DELETED);
        message.setContent("[Message deleted]");
        message.setAttachments(null);
        message = chatMessageRepository.save(message);

        return convertMessageToDTO(message);
    }

    private ConversationDTO convertToDTO(Conversation conversation, String currentUserId) {
        // Get participant info
        List<ConversationDTO.ParticipantInfo> participants = conversation.getParticipantIds().stream()
                .map(id -> {
                    User user = userRepository.findById(id).orElse(null);
                    if (user != null) {
                        return ConversationDTO.ParticipantInfo.builder()
                                .userId(user.getId())
                                .userName(user.getFullName())
                                .avatar(user.getAvatar())
                                .online(false) // TODO: implement online status tracking
                                .build();
                    }
                    return null;
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());

        // Get store info if applicable
        String storeName = null;
        String storeAvatar = null;
        if (conversation.getStoreId() != null) {
            Store store = storeRepository.findById(conversation.getStoreId()).orElse(null);
            if (store != null) {
                storeName = store.getName();
                storeAvatar = store.getLogoUrl();
            }
        }

        // Get unread count for current user
        Integer unreadCount = conversation.getUnreadCounts().stream()
                .filter(count -> count.getUserId().equals(currentUserId))
                .findFirst()
                .map(Conversation.UnreadCount::getCount)
                .orElse(0);

        return ConversationDTO.builder()
                .id(conversation.getId())
                .participants(participants)
                .type(conversation.getType())
                .storeId(conversation.getStoreId())
                .storeName(storeName)
                .storeAvatar(storeAvatar)
                .lastMessage(conversation.getLastMessage())
                .lastMessageTime(conversation.getLastMessageTime())
                .productId(conversation.getProductId())
                .productName(conversation.getProductName())
                .unreadCount(unreadCount)
                .status(conversation.getStatus())
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    private ChatMessageDTO convertMessageToDTO(ChatMessage message) {
        ChatMessageDTO.ProductInfo productInfo = null;

        // If message is product link type, get product info
        if (message.getType() == ChatMessage.MessageType.PRODUCT_LINK && message.getContent() != null) {
            // Content might contain product ID
            Product product = productRepository.findById(message.getContent()).orElse(null);
            if (product != null) {
                // Get image from product variant
                String imageUrl = null;
                // Product doesn't have direct images field, would need to get from variants
                productInfo = ChatMessageDTO.ProductInfo.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .imageUrl(imageUrl)
                        .price(product.getPrice().doubleValue())
                        .build();
            }
        }

        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderAvatar(message.getSenderAvatar())
                .content(message.getContent())
                .type(message.getType())
                .attachments(message.getAttachments())
                .readByUserIds(message.getReadByUserIds())
                .replyToMessageId(message.getReplyToMessageId())
                .status(message.getStatus())
                .sentAt(message.getSentAt())
                .productInfo(productInfo)
                .build();
    }
}
