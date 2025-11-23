package com.example.e_commerce_techshop.dtos.chat;

import com.example.e_commerce_techshop.models.Conversation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateConversationRequest {

    // Optional: for direct user-to-user chat (BUYER_SUPPORT, SELLER_SUPPORT)
    // For BUYER_SELLER type, this will be auto-populated from store owner
    private String recipientId;

    @NotNull(message = "Conversation type is required")
    private Conversation.ConversationType type;

    // Required for BUYER_SELLER type - system will get owner from store
    private String storeId;

    // Optional: start conversation about a specific product
    private String productId;

    // Optional: initial message
    private String initialMessage;
}
