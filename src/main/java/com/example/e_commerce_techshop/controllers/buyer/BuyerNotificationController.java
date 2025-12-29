package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.BuyerNotificationResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/buyer/notifications")
@RequiredArgsConstructor
@Tag(name = "Buyer Notification Management", description = "API cho quản lý thông báo của người mua")
@SecurityRequirement(name = "Bearer Authentication")
public class BuyerNotificationController {

    private final INotificationService notificationService;

    @Operation(summary = "Lấy danh sách notification của buyer", description = "Lấy danh sách phân trang các notification của người mua với tùy chọn lọc theo trạng thái đã đọc")
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @Parameter(description = "Filter by read status (true/false)", required = false) @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) throws Exception {

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(user.getId(), isRead, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications.map(BuyerNotificationResponse::fromNotification).getContent());
        response.put("page", page);
        response.put("size", size);
        response.put("total", notifications.getTotalElements());
        response.put("unreadCount", notificationService.getUserUnreadCount(user.getId()));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Lấy số notification chưa đọc", description = "Lấy số lượng notification chưa đọc của người mua hiện tại")
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User user) throws Exception {
        long unreadCount = notificationService.getUserUnreadCount(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Đánh dấu 1 notification là đã đọc", description = "Đánh dấu một notification cụ thể là đã đọc")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId)
            throws Exception {

        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Đánh dấu thành công"));
    }

    @Operation(summary = "Đánh dấu tất cả notification là đã đọc", description = "Đánh dấu tất cả notification là đã đọc cho người mua hiện tại")
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User user) throws Exception {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đánh dấu tất cả thành công"));
    }

    @Operation(summary = "Xóa 1 notification", description = "Xóa một notification cụ thể")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @Parameter(description = "Notification ID", required = true) @PathVariable String notificationId)
            throws Exception {

        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thành công"));
    }
}
