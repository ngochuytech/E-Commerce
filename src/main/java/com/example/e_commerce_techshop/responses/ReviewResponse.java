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
    private String userId;
    private Integer rating;
    private String comment;
    private String sellerResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional info
    private String customerName;
    private String customerEmail;
    private String productName;
    private String variantName;

    public static ReviewResponse fromReview(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .productVariantId(review.getProductVariant().getId())
                .userId(review.getUser().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                // Seller Response ko có trong DB
//                .sellerResponse(review.getSellerResponse())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}



