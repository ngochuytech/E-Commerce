package com.example.e_commerce_techshop.services.notification;

import com.example.e_commerce_techshop.dtos.NotificationDTO;
import com.example.e_commerce_techshop.models.Notification;

import java.util.List;

public interface INotificationService {

    Notification createNotification(String userId, String title, String message) throws Exception;

    Notification createNotification(NotificationDTO notificationDTO) throws Exception;

    List<Notification> getUserNotifications(String userId, Boolean isRead);

    void markAsRead(String notificationId);

    void markAllAsRead(String userId);
}
