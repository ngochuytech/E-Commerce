package com.example.e_commerce_techshop.services.review;

import com.example.e_commerce_techshop.dtos.ReviewDTO;
import com.example.e_commerce_techshop.dtos.buyer.UpdateReviewDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.*;
import com.example.e_commerce_techshop.repositories.*;
import com.example.e_commerce_techshop.repositories.user.UserRepository;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import com.example.e_commerce_techshop.services.FileUploadService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {
    
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public void createReview(ReviewDTO reviewDTO, List<MultipartFile> images, User currentUser) throws Exception {
        // Kiểm tra order tồn tại và thuộc về user
        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found"));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You can only review your own orders");
        }

        // Kiểm tra order đã hoàn thành chưa
        if (!Order.OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("You can only review completed orders");
        }

        // Kiểm tra product variant tồn tại
        ProductVariant productVariant = productVariantRepository.findById(reviewDTO.getProductVariantId())
                .orElseThrow(() -> new DataNotFoundException("Product variant not found"));

        // Kiểm tra user đã review product variant này trong order này chưa
        List<Review> existingReviews = reviewRepository.findByUserId(currentUser.getId());
        boolean alreadyReviewed = existingReviews.stream()
                .anyMatch(r -> r.getOrder().getId().equals(order.getId()) 
                        && r.getProductVariant().getId().equals(productVariant.getId()));

        if (alreadyReviewed) {
            throw new IllegalArgumentException("You have already reviewed this product in this order");
        }

        List<String > imageUrls = new ArrayList<>();

        if(images != null && !images.isEmpty()) {
            imageUrls = fileUploadService.uploadFiles(images, "reviews");
        }

        // Tạo review mới
        Review review = Review.builder()
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .order(order)
                .productVariant(productVariant)
                .user(currentUser)
                .imageUrls(imageUrls)
                .build();
        order.setRated(true);
        
        orderRepository.save(order);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void updateReview(String reviewId, UpdateReviewDTO reviewDTO, List<MultipartFile> images, User currentUser) throws Exception {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found"));

        // Kiểm tra quyền sở hữu
        if (!existingReview.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You can only update your own reviews");
        }

        // Cập nhật thông tin
        existingReview.setRating(reviewDTO.getRating());
        existingReview.setComment(reviewDTO.getComment());

        // Xử lý upload ảnh mới nếu có
        if (images != null && !images.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (existingReview.getImageUrls() != null && !existingReview.getImageUrls().isEmpty()) {
                for (String imageUrl : existingReview.getImageUrls()) {
                    fileUploadService.deleteFile(imageUrl);
                }
            }
            
            // Upload ảnh mới
            List<String> imageUrls = fileUploadService.uploadFiles(images, "reviews");
            existingReview.setImageUrls(imageUrls);
        }

        reviewRepository.save(existingReview);
    }

    @Override
    public void deleteReview(String reviewId, User currentUser) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Review not found"));

        // Kiểm tra quyền sở hữu
        if (!existingReview.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }

        reviewRepository.delete(existingReview);
    }

    @Override
    public Page<Review> getReviewsByProduct(String productId, Pageable pageable) {
        // Kiểm tra product tồn tại
        productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        
        List<Review> allReviews = reviewRepository.findByProductId(productId);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allReviews.size());
        
        List<Review> pageContent = allReviews.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, allReviews.size());
    }

    @Override
    public List<Review> getReviewsByUser(String userId) {
        // Kiểm tra user tồn tại
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        
        return reviewRepository.findByUserId(userId);
    }

    @Override
    public Map<String, Object> getProductRatingStats(String productVariantId) {
        // Kiểm tra product tồn tại
        productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Product variant not found"));

        List<Review> reviews = reviewRepository.findByProductVariantId(productVariantId);
        
        Map<String, Object> stats = new HashMap<>();
        
        if (reviews.isEmpty()) {
            stats.put("totalReviews", 0);
            stats.put("averageRating", 0.0);
            stats.put("ratingDistribution", Map.of(
                "5", 0, "4", 0, "3", 0, "2", 0, "1", 0
            ));
            return stats;
        }

        // Tính average rating
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Phân bố rating
        Map<String, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(
                    r -> String.valueOf(r.getRating()),
                    Collectors.counting()
                ));

        // Đảm bảo có đủ tất cả rating từ 1-5
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.putIfAbsent(String.valueOf(i), 0L);
        }

        stats.put("totalReviews", reviews.size());
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        stats.put("ratingDistribution", ratingDistribution);
        
        return stats;
    }
    
    @Override
    public List<ReviewResponse> getReviewsByStore(String storeId) {
        List<Review> reviews = reviewRepository.findByOrder_StoreId(storeId);
        return reviews.stream()
                .map(ReviewResponse::fromReview)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<Review> getReviewsByProductVariant(String productVariantId, Pageable pageable) {
        return reviewRepository.findByProductVariantId(productVariantId, pageable);
    }
    
    
    @Override
    public ReviewResponse getReviewById(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
        
        return ReviewResponse.fromReview(review);
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



