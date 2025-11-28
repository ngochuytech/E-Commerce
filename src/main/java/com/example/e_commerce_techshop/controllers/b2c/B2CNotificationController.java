package com.example.e_commerce_techshop.controllers.b2c;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.b2c.B2CNotificationResponse;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.store.IStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/stores/{storeId}/notifications")
@RequiredArgsConstructor
@Tag(name = "B2C Store Notification Management", description = "APIs for store sellers to manage store notifications")
@SecurityRequirement(name = "Bearer Authentication")
public class B2CNotificationController {

    private final INotificationService notificationService;
    private final IStoreService storeService;

    @Operation(summary = "Lấy danh sách notification của store", description = "Retrieve all notifications for a specific store")
    @GetMapping
    public ResponseEntity<?> getStoreNotifications(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId,
            @Parameter(description = "Filter by read status (true/false)", required = false) @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size) throws Exception {

            // Verify store ownership
            verifyStoreOwnership(storeId);

            Pageable pageable = PageRequest.of(page, size);

            Page<Notification> notifications = notificationService.getStoreNotifications(storeId, isRead, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications.map(B2CNotificationResponse::fromNotification).getContent());
            response.put("page", page);
            response.put("size", size);
            response.put("total", notifications.getTotalElements());
            response.put("unreadCount", notificationService.getStoreUnreadCount(storeId));

            return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Lấy số notification chưa đọc của store", description = "Get the number of unread notifications for a specific store")
    @GetMapping("/unread-count")
    public ResponseEntity<?> getStoreUnreadCount(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId) throws Exception {

            verifyStoreOwnership(storeId);

            long unreadCount = notificationService.getStoreUnreadCount(storeId);

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Đánh dấu 1 notification của store là đã đọc", description = "Mark a specific store notification as read")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markStoreNotificationAsRead(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId,
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) throws Exception {

            verifyStoreOwnership(storeId);
            notificationService.markStoreNotificationAsRead(notificationId);
            return ResponseEntity.ok(ApiResponse.ok("Đánh dấu thành công"));
    }

    @Operation(summary = "Đánh dấu tất cả notification của store là đã đọc", description = "Mark all store notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllStoreNotificationsAsRead(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId) throws Exception {

            verifyStoreOwnership(storeId);
            notificationService.markAllStoreNotificationsAsRead(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Đánh dấu tất cả thành công"));
    }

    @Operation(summary = "Xóa 1 notification của store", description = "Delete a specific store notification")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteStoreNotification(
            @Parameter(description = "Store ID", required = true) @PathVariable String storeId,
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId) throws Exception {

            verifyStoreOwnership(storeId);
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa thành công"));
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
