package com.example.e_commerce_techshop.responses;

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
public class NotificationResponse {
    private String id;

    private String title;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private UserResponse user;

    private StoreResponse store;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class UserResponse {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class StoreResponse {
        private String id;
        private String name;
    }

    public static NotificationResponse fromNotification(Notification notification) {
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

        StoreResponse storeResponse = null;
        if (notification.getStore() != null) {
            storeResponse = StoreResponse.builder()
                    .id(notification.getStore().getId())
                    .name(notification.getStore().getName())
                    .build();
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .user(userResponse)
                .store(storeResponse)
                .build();
    }




}
