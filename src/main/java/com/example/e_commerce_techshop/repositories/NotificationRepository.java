package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    // ===== USER NOTIFICATIONS =====
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(String userId, boolean isRead);

    long countByUserIdAndIsRead(String userId, boolean isRead);

    // ===== STORE NOTIFICATIONS =====
    List<Notification> findByStoreIdOrderByCreatedAtDesc(String storeId);

    List<Notification> findByStoreIdAndIsReadOrderByCreatedAtDesc(String storeId, boolean isRead);

    long countByStoreIdAndIsRead(String storeId, boolean isRead);
}
