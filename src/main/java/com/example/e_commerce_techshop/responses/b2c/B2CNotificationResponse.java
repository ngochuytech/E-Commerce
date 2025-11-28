package com.example.e_commerce_techshop.responses.b2c;

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
public class B2CNotificationResponse {
    private String id;

    private String title;

    private String message;

    private Boolean isRead;

    private String relatedId;

    private LocalDateTime createdAt;

    private UserResponse user;
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class UserResponse {
        private String id;
        private String name;
    }

    public static B2CNotificationResponse fromNotification(Notification notification) {
        if (notification == null) {
            return null;
        }

        UserResponse userResponse = null;
        if (notification.getUser() != null) {
            userResponse = UserResponse.builder()
                    .id(notification.getUser().getId())
                    .name(notification.getUser().getFullName())
                    .build();
        }

        return B2CNotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .user(userResponse)
                .relatedId(notification.getRelatedId())
                .build();
    }




}
