package com.example.e_commerce_techshop.services.review;

import com.example.e_commerce_techshop.dtos.ReviewDTO;
import com.example.e_commerce_techshop.models.Review;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IReviewService {
    
    // CRUD operations
    void createReview(ReviewDTO reviewDTO, User currentUser);
    void updateReview(String reviewId, ReviewDTO reviewDTO, User currentUser);
    void deleteReview(String reviewId, User currentUser);
    
    // Get reviews by store
    List<ReviewResponse> getReviewsByStore(String storeId);
    
    // Get reviews by product with pagination
    Page<Review> getReviewsByProduct(String productId, Pageable pageable);
    
    // Get reviews by product variant with pagination
    Page<Review> getReviewsByProductVariant(String productVariantId, Pageable pageable);
    
    // Get reviews by user
    List<Review> getReviewsByUser(String userId);
    
    // Get review by ID
    ReviewResponse getReviewById(String reviewId);
    
    // Get product rating statistics
    Map<String, Object> getProductRatingStats(String productVariantId);
    
    // Get review statistics
    Double getAverageRatingByStore(String storeId);
    Long getReviewCountByStore(String storeId);
    
    // Get reviews by rating
    List<ReviewResponse> getReviewsByRating(String storeId, Integer rating);
    
    // Get recent reviews
    List<ReviewResponse> getRecentReviewsByStore(String storeId, int limit);
}



