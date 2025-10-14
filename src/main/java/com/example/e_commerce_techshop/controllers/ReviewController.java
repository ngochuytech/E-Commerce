package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.ReviewDTO;
import com.example.e_commerce_techshop.models.Review;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import com.example.e_commerce_techshop.services.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Tạo review mới cho sản phẩm
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createReview(
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal User currentUser) {
        try {
            reviewService.createReview(reviewDTO, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Tạo đánh giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách reviews theo product ID
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByProduct(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Review> reviews = reviewService.getReviewsByProduct(productId, pageable);
            Page<ReviewResponse> reviewResponses = reviews.map(ReviewResponse::fromReview);
            
            return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách reviews theo product variant ID
     */
    @GetMapping("/product-variant/{productVariantId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByProductVariant(
            @PathVariable String productVariantId) {
        
        try {
            List<ReviewResponse> reviewResponses = reviewService.getReviewsByProductVariant(productVariantId);
            
            return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách reviews của user hiện tại
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal User currentUser) {
        
        try {
            List<Review> reviews = reviewService.getReviewsByUser(currentUser.getId());
            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(ReviewResponse::fromReview)
                    .toList();
            
            return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật review
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            reviewService.updateReview(reviewId, reviewDTO, currentUser);
            
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật đánh giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa review
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @PathVariable String reviewId,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            reviewService.deleteReview(reviewId, currentUser);
            
            return ResponseEntity.ok(ApiResponse.ok("Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy thông tin chi tiết một review
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @PathVariable String reviewId) {
        
        try {
            ReviewResponse reviewResponse = reviewService.getReviewById(reviewId);
            
            return ResponseEntity.ok(ApiResponse.ok(reviewResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy thống kê rating cho một sản phẩm
     */
    @GetMapping("/product-variant/{productVariantId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductRatingStats(
            @PathVariable String productVariantId) {
        
        try {
            Map<String, Object> stats = reviewService.getProductRatingStats(productVariantId);
            
            return ResponseEntity.ok(ApiResponse.ok(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}