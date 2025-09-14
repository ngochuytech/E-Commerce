package com.example.e_commerce_techshop.services.review;

import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Review;
import com.example.e_commerce_techshop.repositories.ReviewRepository;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {
    
    private final ReviewRepository reviewRepository;
    
    @Override
    public List<ReviewResponse> getReviewsByStore(String storeId) {
        List<Review> reviews = reviewRepository.findByStoreId(storeId);
        return reviews.stream()
                .map(ReviewResponse::fromReview)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewResponse> getReviewsByProduct(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return reviews.stream()
                .map(ReviewResponse::fromReview)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewResponse> getReviewsByProductVariant(String productVariantId) {
        List<Review> reviews = reviewRepository.findByProductVariantId(productVariantId);
        return reviews.stream()
                .map(ReviewResponse::fromReview)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReviewResponse getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
        
        return ReviewResponse.fromReview(review);
    }
    
    @Override
    public ReviewResponse respondToReview(String reviewId, String response) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
        
        review.setSellerResponse(response);
        Review updatedReview = reviewRepository.save(review);
        
        return ReviewResponse.fromReview(updatedReview);
    }
    
    @Override
    public List<ReviewResponse> getPendingReviewsByStore(String storeId) {
        List<Review> reviews = reviewRepository.findPendingReviewsByStoreId(storeId);
        return reviews.stream()
                .map(ReviewResponse::fromReview)
                .collect(Collectors.toList());
    }
    
    @Override
    public Double getAverageRatingByStore(String storeId) {
        return reviewRepository.getAverageRatingByStoreId(storeId);
    }
    
    @Override
    public Long getReviewCountByStore(String storeId) {
        return reviewRepository.countByStoreId(storeId);
    }
    
    @Override
    public List<ReviewResponse> getReviewsByRating(String storeId, Integer rating) {
        List<ReviewResponse> allReviews = getReviewsByStore(storeId);
        return allReviews.stream()
                .filter(review -> review.getRating().equals(rating))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewResponse> getRecentReviewsByStore(String storeId, int limit) {
        List<ReviewResponse> reviews = getReviewsByStore(storeId);
        return reviews.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}



