package com.example.e_commerce_techshop.controllers;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Review;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import com.example.e_commerce_techshop.services.review.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "API cho quản lý đánh giá sản phẩm")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Lấy đánh giá theo sản phẩm", description = "Lấy danh sách đánh giá phân trang cho một sản phẩm cụ thể với các tùy chọn sắp xếp")
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

    @GetMapping("/product-variant/{productVariantId}")
    @Operation(summary = "Lấy đánh giá theo biến thể sản phẩm", description = "Lấy danh sách đánh giá phân trang cho một biến thể sản phẩm cụ thể với các tùy chọn sắp xếp")
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

    @GetMapping("/{reviewId}")
    @Operation(summary = "Lấy đánh giá theo ID", description = "Lấy thông tin chi tiết của một đánh giá cụ thể")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String reviewId)
            throws Exception {
        ReviewResponse reviewResponse = reviewService.getReviewById(reviewId);

        return ResponseEntity.ok(ApiResponse.ok(reviewResponse));
    }

    @GetMapping("/product-variant/{productVariantId}/stats")
    @Operation(summary = "Lấy thống kê đánh giá sản phẩm", description = "Lấy thống kê đánh giá cho một biến thể sản phẩm bao gồm điểm đánh giá trung bình, tổng số đánh giá và phân bố đánh giá")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductRatingStats(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String productVariantId)
            throws Exception {
        Map<String, Object> stats = reviewService.getProductRatingStats(productVariantId);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}