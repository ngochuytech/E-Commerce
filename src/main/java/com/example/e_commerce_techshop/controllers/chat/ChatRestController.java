package com.example.e_commerce_techshop.controllers.chat;

import com.example.e_commerce_techshop.dtos.chat.*;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.services.chat.IChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API cho chức năng trò chuyện giữa người dùng và cửa hàng")
@SecurityRequirement(name = "bearerAuth")
public class ChatRestController {

    private final IChatService chatService;

    @GetMapping("/conversations")
    @Operation(summary = "Lấy các cuộc trò chuyện của người dùng với phân trang")
    public ResponseEntity<Page<ConversationDTO>> getUserConversations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageTime").descending());
        Page<ConversationDTO> conversations = chatService.getUserConversations(currentUser.getId(), pageable);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Lấy chi tiết cuộc trò chuyện")
    public ResponseEntity<ConversationDTO> getConversation(
            @PathVariable String conversationId,
            @AuthenticationPrincipal User currentUser) {

        ConversationDTO conversation = chatService.getConversation(conversationId, currentUser.getId());
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations/unread-count")
    @Operation(summary = "Lấy số lượng cuộc trò chuyện có tin nhắn chưa đọc")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {

        Long count = chatService.getUnreadConversationsCount(currentUser.getId());

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Lấy các tin nhắn trong cuộc trò chuyện với phân trang")
    public ResponseEntity<Page<ChatMessageDTO>> getConversationMessages(
            @PathVariable String conversationId,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<ChatMessageDTO> messages = chatService.getConversationMessages(
                conversationId, currentUser.getId(), pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations/find-or-create")
    @Operation(summary = "Tìm cuộc trò chuyện hiện có hoặc tạo mới giữa các người dùng")
    public ResponseEntity<ConversationDTO> findOrCreateConversation(
            @RequestParam String storeId,
            @RequestParam String recipientId,
            @AuthenticationPrincipal User currentUser) {

        ConversationDTO conversation = chatService.getOrCreateConversation(
                currentUser.getId(), recipientId, storeId);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/conversations")
    @Operation(summary = "Tạo 1 cuộc trò chuyện mới")
    public ResponseEntity<ConversationDTO> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal User currentUser) {

        ConversationDTO conversation = chatService.createConversation(request, currentUser.getId());
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/conversations/{conversationId}/archive")
    @Operation(summary = "Lưu trữ cuộc trò chuyện")
    public ResponseEntity<Map<String, String>> archiveConversation(
            @PathVariable String conversationId,
            @AuthenticationPrincipal User currentUser) {

        chatService.archiveConversation(conversationId, currentUser.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Conversation archived successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/messages")
    @Operation(summary = "Gửi tin nhắn (REST endpoint, ưu tiên WebSocket)")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User currentUser) {

        ChatMessageDTO message = chatService.sendMessage(request, currentUser.getId());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/messages/{messageId}/read")
    @Operation(summary = "Đánh dấu tin nhắn là đã đọc")
    public ResponseEntity<Map<String, String>> markMessageAsRead(
            @PathVariable String messageId,
            @AuthenticationPrincipal User currentUser) {

        chatService.markMessageAsRead(messageId, currentUser.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Message marked as read");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{conversationId}/read")
    @Operation(summary = "Đánh dấu tất cả tin nhắn trong cuộc trò chuyện là đã đọc")
    public ResponseEntity<Map<String, String>> markConversationAsRead(
            @PathVariable String conversationId,
            @AuthenticationPrincipal User currentUser) {

        chatService.markConversationAsRead(conversationId, currentUser.getId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Conversation marked as read");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Xóa một tin nhắn")
    public ResponseEntity<ChatMessageDTO> deleteMessage(
            @PathVariable String messageId,
            @AuthenticationPrincipal User currentUser) {

        ChatMessageDTO message = chatService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.ok(message);
    }
}
