package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.models.Review;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import com.example.e_commerce_techshop.services.review.ReviewService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "APIs for product reviews and rating statistics")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Lấy danh sách reviews theo product ID
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reviews by product", description = "Retrieve paginated reviews for a specific product with sorting options")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByProduct(
            @Parameter(description = "Product ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String productId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews = reviewService.getReviewsByProduct(productId, pageable);
        Page<ReviewResponse> reviewResponses = reviews.map(ReviewResponse::fromReview);

        return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
    }

    /**
     * Lấy danh sách reviews theo product variant ID
     */
    @GetMapping("/product-variant/{productVariantId}")
    @Operation(summary = "Get reviews by product variant", description = "Retrieve paginated reviews for a specific product variant with sorting options")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByProductVariant(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String productVariantId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews = reviewService.getReviewsByProductVariant(productVariantId, pageable);
        Page<ReviewResponse> reviewResponses = reviews.map(ReviewResponse::fromReview);

        return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
    }

    /**
     * Lấy danh sách reviews của user hiện tại
     */
    @GetMapping("/my-reviews")
    @Operation(summary = "Get my reviews", description = "Retrieve all reviews created by the current authenticated user")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal User currentUser) throws Exception {

        List<Review> reviews = reviewService.getReviewsByUser(currentUser.getId());
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(ReviewResponse::fromReview)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
    }

    /**
     * Lấy thông tin chi tiết một review
     */
    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Retrieve detailed information of a specific review")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String reviewId)
            throws Exception {
        ReviewResponse reviewResponse = reviewService.getReviewById(reviewId);

        return ResponseEntity.ok(ApiResponse.ok(reviewResponse));
    }

    /**
     * Lấy thống kê rating cho một sản phẩm
     */
    @GetMapping("/product-variant/{productVariantId}/stats")
    @Operation(summary = "Get product rating statistics", description = "Get rating statistics for a product variant including average rating, total reviews, and rating distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductRatingStats(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String productVariantId)
            throws Exception {
        Map<String, Object> stats = reviewService.getProductRatingStats(productVariantId);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}