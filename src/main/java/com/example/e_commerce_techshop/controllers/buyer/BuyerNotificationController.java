package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/buyer/notifications")
@RequiredArgsConstructor
@Tag(name = "Buyer Notification Management", description = "APIs for buyers to manage their notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class BuyerNotificationController {

    private final INotificationService notificationService;

    /**
     * Lấy danh sách notification của buyer
     */
    @Operation(summary = "Lấy danh sách notification của buyer", description = "Retrieve all notifications for the current buyer")
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @Parameter(description = "Filter by read status (true/false)", required = false) @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        try {
            List<Notification> notifications = notificationService.getUserNotifications(user.getId(), isRead);

            // Phân trang
            int start = page * size;
            int end = Math.min(start + size, notifications.size());
            List<Notification> paginatedNotifications = notifications.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", paginatedNotifications);
            response.put("page", page);
            response.put("size", size);
            response.put("total", notifications.size());
            response.put("unreadCount", notificationService.getUserUnreadCount(user.getId()));

            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy số notification chưa đọc
     */
    @Operation(summary = "Lấy số notification chưa đọc", description = "Get the number of unread notifications for the current buyer")
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User user) {
        try {
            long unreadCount = notificationService.getUserUnreadCount(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Đánh dấu 1 notification là đã đọc
     */
    @Operation(summary = "Đánh dấu 1 notification là đã đọc", description = "Mark a specific notification as read")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) {

        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(ApiResponse.ok("Đánh dấu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Đánh dấu tất cả notification là đã đọc
     */
    @Operation(summary = "Đánh dấu tất cả notification là đã đọc", description = "Mark all notifications as read for the current buyer")
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User user) {
        try {
            notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok(ApiResponse.ok("Đánh dấu tất cả thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa 1 notification
     */
    @Operation(summary = "Xóa 1 notification", description = "Delete a specific notification")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) {

        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
