package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.store.IStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/b2c/stores/{storeId}/notifications")
@RequiredArgsConstructor
@Tag(name = "B2C Store Notification Management", description = "APIs for store sellers to manage store notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class B2CNotificationController {

    private final INotificationService notificationService;
    private final IStoreService storeService;

    /**
     * Lấy danh sách notification của store
     */
    @Operation(summary = "Lấy danh sách notification của store", description = "Retrieve all notifications for a specific store")
    @GetMapping
    public ResponseEntity<?> getStoreNotifications(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId,
            @Parameter(description = "Filter by read status (true/false)", required = false) @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size) {

        try {
            // Verify store ownership
            verifyStoreOwnership(storeId);

            List<Notification> notifications = notificationService.getStoreNotifications(storeId, isRead);

            // Phân trang
            int start = page * size;
            int end = Math.min(start + size, notifications.size());
            List<Notification> paginatedNotifications = notifications.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", paginatedNotifications);
            response.put("page", page);
            response.put("size", size);
            response.put("total", notifications.size());
            response.put("unreadCount", notificationService.getStoreUnreadCount(storeId));

            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy số notification chưa đọc của store
     */
    @Operation(summary = "Lấy số notification chưa đọc của store", description = "Get the number of unread notifications for a specific store")
    @GetMapping("/unread-count")
    public ResponseEntity<?> getStoreUnreadCount(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId) {

        try {
            verifyStoreOwnership(storeId);

            long unreadCount = notificationService.getStoreUnreadCount(storeId);

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Đánh dấu 1 notification của store là đã đọc
     */
    @Operation(summary = "Đánh dấu 1 notification của store là đã đọc", description = "Mark a specific store notification as read")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markStoreNotificationAsRead(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId,
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) {

        try {
            verifyStoreOwnership(storeId);
            notificationService.markStoreNotificationAsRead(notificationId);
            return ResponseEntity.ok(ApiResponse.ok("Đánh dấu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Đánh dấu tất cả notification của store là đã đọc
     */
    @Operation(summary = "Đánh dấu tất cả notification của store là đã đọc", description = "Mark all store notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllStoreNotificationsAsRead(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId) {

        try {
            verifyStoreOwnership(storeId);
            notificationService.markAllStoreNotificationsAsRead(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Đánh dấu tất cả thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa 1 notification của store
     */
    @Operation(summary = "Xóa 1 notification của store", description = "Delete a specific store notification")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteStoreNotification(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId,
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) {

        try {
            verifyStoreOwnership(storeId);
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Verify store ownership - đảm bảo user là owner của store
     */
    private void verifyStoreOwnership(String storeId) throws Exception {
        User user = getCurrentUser();
        storeService.getStoreByIdAndOwnerId(storeId, user.getId());
    }

    /**
     * Lấy thông tin user hiện tại
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
