package com.example.e_commerce_techshop.services.review;

import com.example.e_commerce_techshop.responses.ReviewResponse;

import java.util.List;

public interface IReviewService {
    
    // Get reviews by store
    List<ReviewResponse> getReviewsByStore(String storeId);
    
    // Get reviews by product
    List<ReviewResponse> getReviewsByProduct(String productId);
    
    // Get reviews by product variant
    List<ReviewResponse> getReviewsByProductVariant(String productVariantId);
    
    // Get review by ID
    ReviewResponse getReviewById(String reviewId);
    
    // Respond to review
    ReviewResponse respondToReview(String reviewId, String response);
    
    // Get pending reviews (without seller response)
    List<ReviewResponse> getPendingReviewsByStore(String storeId);
    
    // Get review statistics
    Double getAverageRatingByStore(String storeId);
    Long getReviewCountByStore(String storeId);
    
    // Get reviews by rating
    List<ReviewResponse> getReviewsByRating(String storeId, Integer rating);
    
    // Get recent reviews
    List<ReviewResponse> getRecentReviewsByStore(String storeId, int limit);
}



