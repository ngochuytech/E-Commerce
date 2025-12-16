package com.example.e_commerce_techshop.responses.admin;

import java.time.LocalDateTime;

import com.example.e_commerce_techshop.models.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminNotificationResponse {
    private String id;

    private String title;

    private String message;

    private Notification.NotificationType type; // ORDER_UPDATE, STORE_APPROVAL, PRODUCT_APPROVAL, PAYMENT, SYSTEM

    private String relatedId; // Order ID, Store ID, Product ID, etc.

    private Boolean isRead;

    private LocalDateTime createdAt;

    public static AdminNotificationResponse fromNotification(Notification notification) {
        if (notification == null) {
            return null;
        }

        return AdminNotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .relatedId(notification.getRelatedId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
