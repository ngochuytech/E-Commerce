package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // ===== ADMIN NOTIFICATIONS =====
    List<Notification> findByIsAdminAndIsReadOrderByCreatedAtDesc(boolean isAdmin, boolean isRead);

    List<Notification> findByIsAdminOrderByCreatedAtDesc(boolean isAdmin);

    long countByIsAdminAndIsRead(boolean isAdmin, boolean isRead);
    
    // Pagination methods for admin notifications
    Page<Notification> findByIsAdminOrderByCreatedAtDesc(boolean isAdmin, Pageable pageable);
    
    Page<Notification> findByIsAdminAndIsReadOrderByCreatedAtDesc(boolean isAdmin, boolean isRead, Pageable pageable);
    
    Page<Notification> findByIsAdminAndTypeOrderByCreatedAtDesc(boolean isAdmin, String type, Pageable pageable);
}
