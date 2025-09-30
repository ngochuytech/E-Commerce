package com.example.e_commerce_techshop.services.notification;

import com.example.e_commerce_techshop.dtos.NotificationDTO;
import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.NotificationRepository;
import com.example.e_commerce_techshop.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService{
    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    @Override
    public Notification createNotification(String userId, String title, String message) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotification(NotificationDTO notificationDTO) {
        User user = userRepository.findById(notificationDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + notificationDTO.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .title(notificationDTO.getTitle())
                .message(notificationDTO.getMessage())
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(String userId, Boolean isRead) {
        if (isRead != null) {
            return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
}
