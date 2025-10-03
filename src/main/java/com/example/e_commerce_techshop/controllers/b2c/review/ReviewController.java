package com.example.e_commerce_techshop.controllers.b2c.review;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import com.example.e_commerce_techshop.services.review.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final IReviewService reviewService;
    
    // Get reviews by store
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getReviewsByStore(@PathVariable String storeId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok(reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get reviews by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getReviewsByProduct(@PathVariable String productId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId);
            return ResponseEntity.ok(ApiResponse.ok(reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get reviews by product variant
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<?> getReviewsByProductVariant(@PathVariable String variantId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByProductVariant(variantId);
            return ResponseEntity.ok(ApiResponse.ok(reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get review by ID
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable String reviewId) {
        try {
            ReviewResponse review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(ApiResponse.ok(review));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Respond to review
    @PutMapping("/{reviewId}/respond")
    public ResponseEntity<?> respondToReview(@PathVariable String reviewId, @RequestParam String response) {
        try {
            ReviewResponse review = reviewService.respondToReview(reviewId, response);
            return ResponseEntity.ok(ApiResponse.ok("Phản hồi đánh giá thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get pending reviews
    @GetMapping("/store/{storeId}/pending")
    public ResponseEntity<?> getPendingReviews(@PathVariable String storeId) {
        try {
            List<ReviewResponse> reviews = reviewService.getPendingReviewsByStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok(reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get review statistics
    @GetMapping("/store/{storeId}/statistics")
    public ResponseEntity<?> getReviewStatistics(@PathVariable String storeId) {
        try {
            Double averageRating = reviewService.getAverageRatingByStore(storeId);
            Long totalReviews = reviewService.getReviewCountByStore(storeId);
            
            return ResponseEntity.ok(ApiResponse.ok(
                String.format("Đánh giá trung bình: %.1f/5.0, Tổng số đánh giá: %d", 
                    averageRating != null ? averageRating : 0.0, totalReviews)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get reviews by rating
    @GetMapping("/store/{storeId}/rating/{rating}")
    public ResponseEntity<?> getReviewsByRating(@PathVariable String storeId, @PathVariable Integer rating) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByRating(storeId, rating);
            return ResponseEntity.ok(ApiResponse.ok(reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get recent reviews
    @GetMapping("/store/{storeId}/recent")
    public ResponseEntity<?> getRecentReviews(@PathVariable String storeId, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ReviewResponse> reviews = reviewService.getRecentReviewsByStore(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok(reviews));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}




