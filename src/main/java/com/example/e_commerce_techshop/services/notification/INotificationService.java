package com.example.e_commerce_techshop.services.notification;

import com.example.e_commerce_techshop.dtos.NotificationDTO;
import com.example.e_commerce_techshop.models.Notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // ===== ADMIN NOTIFICATIONS =====
    /**
     * Tạo notification cho admin
     */
    Notification createAdminNotification(String title, String message, String type, String relatedId) throws Exception;

    /**
     * Lấy tất cả notification của admin
     */
    List<Notification> getAdminNotifications(Boolean isRead);

    /**
     * Đánh dấu notification của admin là đã đọc
     */
    void markAdminNotificationAsRead(String notificationId);

    /**
     * Đánh dấu tất cả notification của admin là đã đọc
     */
    void markAllAdminNotificationsAsRead();

    /**
     * Lấy số notification chưa đọc của admin
     */
    long getAdminUnreadCount();

    /**
     * Lấy danh sách admin notifications với phân trang
     */
    Page<Notification> getAdminNotificationsPage(Pageable pageable);

    /**
     * Lấy danh sách admin notifications theo trạng thái với phân trang
     */
    Page<Notification> getAdminNotificationsPage(Boolean isRead, Pageable pageable);

    /**
     * Lấy danh sách admin notifications theo type với phân trang
     */
    Page<Notification> getAdminNotificationsByTypePage(String type, Pageable pageable);
}
