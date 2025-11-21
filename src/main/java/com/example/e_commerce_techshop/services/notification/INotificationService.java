package com.example.e_commerce_techshop.services.notification;

import com.example.e_commerce_techshop.dtos.NotificationDTO;
import com.example.e_commerce_techshop.models.Notification;

import java.util.List;

public interface INotificationService {

    // ===== USER NOTIFICATIONS =====
    /**
     * Tạo notification cho user
     */
    Notification createUserNotification(String userId, String title, String message) throws Exception;

    /**
     * Tạo notification cho user từ DTO
     */
    Notification createUserNotification(NotificationDTO notificationDTO) throws Exception;

    /**
     * Lấy notification của user
     */
    List<Notification> getUserNotifications(String userId, Boolean isRead);

    /**
     * Đánh dấu notification là đã đọc
     */
    void markAsRead(String notificationId);

    /**
     * Đánh dấu tất cả notification của user là đã đọc
     */
    void markAllAsRead(String userId);

    // ===== STORE NOTIFICATIONS =====
    /**
     * Tạo notification cho store
     */
    Notification createStoreNotification(String storeId, String title, String message) throws Exception;

    /**
     * Lấy notification của store
     */
    List<Notification> getStoreNotifications(String storeId, Boolean isRead);

    /**
     * Đánh dấu notification của store là đã đọc
     */
    void markStoreNotificationAsRead(String notificationId);

    /**
     * Đánh dấu tất cả notification của store là đã đọc
     */
    void markAllStoreNotificationsAsRead(String storeId);

    /**
     * Xóa notification
     */
    void deleteNotification(String notificationId);

    /**
     * Lấy số notification chưa đọc của user
     */
    long getUserUnreadCount(String userId);

    /**
     * Lấy số notification chưa đọc của store
     */
    long getStoreUnreadCount(String storeId);
}
