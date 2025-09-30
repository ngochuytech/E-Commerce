package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(String userId, boolean isRead);
}
