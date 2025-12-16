package com.example.e_commerce_techshop.responses.buyer;

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
public class BuyerNotificationResponse {
    private String id;

    private String title;

    private String message;

    private Boolean isRead;

    private String relatedId;

    private LocalDateTime createdAt;

    public static BuyerNotificationResponse fromNotification(Notification notification) {
        if (notification == null) {
            return null;
        }

        return BuyerNotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .relatedId(notification.getRelatedId())
                .build();
    }
}
