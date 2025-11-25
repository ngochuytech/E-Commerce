package com.example.e_commerce_techshop.services.notification;

import com.example.e_commerce_techshop.dtos.NotificationDTO;
import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.models.Notification.NotificationType;
import com.example.e_commerce_techshop.repositories.NotificationRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.repositories.user.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    // ===== USER NOTIFICATIONS =====

    @Override
    public Notification createUserNotification(String userId, String title, String message) throws Exception {
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

    @Override
    @Transactional
    public Notification createUserNotification(NotificationDTO notificationDTO) throws Exception {
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

    @Override
    public List<Notification> getUserNotifications(String userId, Boolean isRead) {
        if (isRead != null) {
            return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public long getUserUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    // ===== STORE NOTIFICATIONS =====

    @Override
    public Notification createStoreNotification(String storeId, String title, String message) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        Notification notification = Notification.builder()
                .store(store)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getStoreNotifications(String storeId, Boolean isRead) {
        if (isRead != null) {
            return notificationRepository.findByStoreIdAndIsReadOrderByCreatedAtDesc(storeId, isRead);
        }
        return notificationRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
    }

    @Override
    @Transactional
    public void markStoreNotificationAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllStoreNotificationsAsRead(String storeId) {
        List<Notification> notifications = notificationRepository.findByStoreIdAndIsReadOrderByCreatedAtDesc(storeId, false);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteNotification(String notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public long getStoreUnreadCount(String storeId) {
        return notificationRepository.countByStoreIdAndIsRead(storeId, false);
    }

    // ===== ADMIN NOTIFICATIONS =====

    @Override
    @Transactional
    public Notification createAdminNotification(String title, String message, String type, String relatedId) throws Exception {
        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(NotificationType.valueOf(type))
                .relatedId(relatedId)
                .isAdmin(true)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getAdminNotifications(Boolean isRead) {
        if (isRead != null) {
            return notificationRepository.findByIsAdminAndIsReadOrderByCreatedAtDesc(true, isRead);
        }
        return notificationRepository.findByIsAdminOrderByCreatedAtDesc(true);
    }

    @Override
    @Transactional
    public void markAdminNotificationAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        if (!notification.getIsAdmin()) {
            throw new RuntimeException("This is not an admin notification");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAdminNotificationsAsRead() {
        List<Notification> notifications = notificationRepository.findByIsAdminAndIsReadOrderByCreatedAtDesc(true, false);
        notifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public long getAdminUnreadCount() {
        return notificationRepository.countByIsAdminAndIsRead(true, false);
    }

    @Override
    public Page<Notification> getAdminNotificationsPage(Pageable pageable) {
        return notificationRepository.findByIsAdminOrderByCreatedAtDesc(true, pageable);
    }

    @Override
    public Page<Notification> getAdminNotificationsPage(Boolean isRead, Pageable pageable) {
        if (isRead != null) {
            return notificationRepository.findByIsAdminAndIsReadOrderByCreatedAtDesc(true, isRead, pageable);
        }
        return notificationRepository.findByIsAdminOrderByCreatedAtDesc(true, pageable);
    }

    @Override
    public Page<Notification> getAdminNotificationsByTypePage(String type, Pageable pageable) {
        return notificationRepository.findByIsAdminAndTypeOrderByCreatedAtDesc(true, type, pageable);
    }
}
