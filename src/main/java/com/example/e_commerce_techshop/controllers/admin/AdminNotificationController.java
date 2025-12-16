package com.example.e_commerce_techshop.controllers.admin;

import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.admin.AdminNotificationResponse;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin Notification Management", description = "APIs for admin to manage platform notifications")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final INotificationService notificationService;

    @Operation(summary = "Lấy danh sách notification của admin", description = "Retrieve all notifications for the admin")
    @GetMapping
    public ResponseEntity<?> getAdminNotifications(
            @Parameter(description = "Filter by read status (true/false)", required = false) @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) throws Exception {

            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notificationPage;
            
            if (isRead != null) {
                notificationPage = notificationService.getAdminNotificationsPage(isRead, pageable);
            } else {
                notificationPage = notificationService.getAdminNotificationsPage(pageable);
            }

            Page<AdminNotificationResponse> adminNotificationResponses = notificationPage.map(AdminNotificationResponse::fromNotification);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", adminNotificationResponses.getContent());
            response.put("page", page);
            response.put("size", size);
            response.put("total", notificationPage.getTotalElements());
            response.put("totalPages", notificationPage.getTotalPages());
            response.put("unreadCount", notificationService.getAdminUnreadCount());

            return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Lấy số notification chưa đọc của admin", description = "Get the number of unread notifications for admin")
    @GetMapping("/unread-count")
    public ResponseEntity<?> getAdminUnreadCount(@AuthenticationPrincipal User user) throws Exception {
            long unreadCount = notificationService.getAdminUnreadCount();

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);

            return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Đánh dấu notification là đã đọc", description = "Mark a specific admin notification as read")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId,
            @AuthenticationPrincipal User user) throws Exception{
                
            notificationService.markAdminNotificationAsRead(notificationId);

            return ResponseEntity.ok(ApiResponse.ok("Đã đánh dấu thông báo là đã đọc"));
    }

    @Operation(summary = "Đánh dấu tất cả notification là đã đọc", description = "Mark all admin notifications as read")
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead(@AuthenticationPrincipal User user) throws Exception{
            notificationService.markAllAdminNotificationsAsRead();

            return ResponseEntity.ok(ApiResponse.ok("Đã đánh dấu tất cả thông báo là đã đọc"));
    }

    @Operation(summary = "Xóa notification", description = "Delete a specific admin notification")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId,
            @AuthenticationPrincipal User user) throws Exception{

            notificationService.deleteNotification(notificationId);

            return ResponseEntity.ok(ApiResponse.ok("Đã xóa thông báo thành công"));
    }

    @Operation(summary = "Lấy notification theo id", description = "Get a specific admin notification by ID")
    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotificationById(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId,
            @AuthenticationPrincipal User user) throws Exception {

            Notification notification = notificationService.getAdminNotifications(null)
                    .stream()
                    .filter(n -> n.getId().equals(notificationId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Notification not found"));

            return ResponseEntity.ok(ApiResponse.ok(AdminNotificationResponse.fromNotification(notification)));
    }

    @Operation(summary = "Lấy notification theo type", description = "Get all admin notifications filtered by type")
    @GetMapping("/by-type/{type}")
    public ResponseEntity<?> getNotificationsByType(
            @Parameter(description = "Notification type (STORE_APPROVAL, PRODUCT_APPROVAL, WITHDRAWAL_REQUEST, SYSTEM)", required = true) @PathVariable String type,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) throws Exception {

            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notificationPage = notificationService.getAdminNotificationsByTypePage(type, pageable);
            Page<AdminNotificationResponse> adminNotificationResponses = notificationPage.map(AdminNotificationResponse::fromNotification);

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", adminNotificationResponses.getContent());
            response.put("type", type);
            response.put("page", notificationPage.getNumber());
            response.put("size", notificationPage.getSize());
            response.put("total", notificationPage.getTotalElements());
            response.put("totalPages", notificationPage.getTotalPages());

            return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
