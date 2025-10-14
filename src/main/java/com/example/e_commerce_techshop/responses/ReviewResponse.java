package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Review;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse {
    private String id;
    private String orderId;
    private String productVariantId;
    private Integer rating;
    private String comment;
    private UserResponse user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private String id;
        private String name;
        private String email;
        private String avatar;
    }

    public static ReviewResponse fromReview(Review review) {
        UserResponse userResponse = UserResponse.builder()
                .id(review.getUser().getId())
                .name(review.getUser().getFullName())
                .email(review.getUser().getEmail())
                .avatar(review.getUser().getAvatarUrl())
                .build();

        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .productVariantId(review.getProductVariant().getId())
                .user(userResponse)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}



