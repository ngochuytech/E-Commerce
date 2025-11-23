package com.example.e_commerce_techshop.controllers.chat;

import com.example.e_commerce_techshop.dtos.chat.*;
import com.example.e_commerce_techshop.services.chat.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final IChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle sending messages via WebSocket
     * Client sends to: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userId = getUserIdFromUserDetails(userDetails);
            
            // Service will handle saving and broadcasting
            chatService.sendMessage(request, userId);
        }
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(@Payload TypingIndicatorRequest request, Authentication authentication) {
        if (authentication != null) {
            // Broadcast typing indicator to conversation participants
            messagingTemplate.convertAndSend(
                "/topic/conversation/" + request.getConversationId() + "/typing",
                request
            );
        }
    }

    /**
     * Handle read receipts
     * Client sends to: /app/chat.markRead
     */
    @MessageMapping("/chat.markRead")
    public void markAsRead(@Payload MessageReadReceiptRequest request, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userId = getUserIdFromUserDetails(userDetails);
            
            if (request.getMessageId() != null) {
                chatService.markMessageAsRead(request.getMessageId(), userId);
            } else if (request.getConversationId() != null) {
                chatService.markConversationAsRead(request.getConversationId(), userId);
            }
        }
    }

    /**
     * Handle user presence (online/offline status)
     * Client sends to: /app/chat.userPresence
     */
    @MessageMapping("/chat.userPresence")
    public void handleUserPresence(@Payload UserPresenceRequest request, Authentication authentication) {
        if (authentication != null) {
            // Broadcast presence status
            messagingTemplate.convertAndSend(
                "/topic/user/" + request.getUserId() + "/presence",
                request
            );
        }
    }

    private String getUserIdFromUserDetails(UserDetails userDetails) {
        // Assuming UserDetails is your custom User entity that has getId()
        if (userDetails instanceof com.example.e_commerce_techshop.models.User) {
            return ((com.example.e_commerce_techshop.models.User) userDetails).getId();
        }
        return userDetails.getUsername(); // Fallback
    }

    // DTO for user presence
    public static class UserPresenceRequest {
        private String userId;
        private Boolean online;
        private String status; // "online", "offline", "away"

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Boolean getOnline() { return online; }
        public void setOnline(Boolean online) { this.online = online; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
