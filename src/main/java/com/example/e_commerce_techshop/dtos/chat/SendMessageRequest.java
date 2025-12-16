package com.example.e_commerce_techshop.dtos.chat;

import com.example.e_commerce_techshop.models.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Conversation ID is required")
    private String conversationId;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Message type is required")
    private ChatMessage.MessageType type;

    // Optional: for image/file messages
    private List<String> attachments;

    // Optional: reply to another message
    private String replyToMessageId;

    // Optional: share product in chat
    private String productId;
}
