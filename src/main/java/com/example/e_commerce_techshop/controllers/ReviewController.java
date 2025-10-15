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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
     * Tạo review mới cho sản phẩm
     */
    @PostMapping
    @Operation(summary = "Create product review", 
               description = "Create a new review for a product with rating and comment")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Review created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid data or user cannot review this product",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<?>> createReview(
            @Parameter(description = "Review information including rating, comment, and product variant ID")
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
    @Operation(summary = "Get reviews by product", 
               description = "Retrieve paginated reviews for a specific product with sorting options")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reviews retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Product not found or invalid parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByProduct(
            @Parameter(description = "Product ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String productId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc")
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
    @Operation(summary = "Get reviews by product variant", 
               description = "Retrieve all reviews for a specific product variant")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reviews retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Product variant not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByProductVariant(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
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
    @Operation(summary = "Get my reviews", 
               description = "Retrieve all reviews created by the current authenticated user")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User reviews retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "User not authenticated or error retrieving reviews",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
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
    @Operation(summary = "Update review", 
               description = "Update an existing review (only by the review owner)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Review not found or user not authorized",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<?>> updateReview(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String reviewId,
            @Parameter(description = "Updated review information")
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
    @Operation(summary = "Delete review", 
               description = "Delete a review (only by the review owner)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Review not found or user not authorized",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
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
    @Operation(summary = "Get review by ID", 
               description = "Retrieve detailed information of a specific review")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReviewResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
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
    @Operation(summary = "Get product rating statistics", 
               description = "Get rating statistics for a product variant including average rating, total reviews, and rating distribution")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Rating statistics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Product variant not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductRatingStats(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
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