package com.example.e_commerce_techshop.controllers.buyer;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.annotations.RequireActiveAccount;
import com.example.e_commerce_techshop.dtos.ReviewDTO;
import com.example.e_commerce_techshop.models.Review;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ReviewResponse;
import com.example.e_commerce_techshop.services.review.IReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/buyer/reviews")
@RequiredArgsConstructor
@RequireActiveAccount
@Tag(name = "Buyer Review Management", description = "APIs for buyer review operations")
@SecurityRequirement(name = "bearerAuth")
public class BuyerReviewController {

    private final IReviewService reviewService;

    /**
     * Tạo review mới cho sản phẩm
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create product review", description = "Create a new review for a product with rating and comment")
    public ResponseEntity<?> createReview(
            @Parameter(description = "Review information including rating, comment, and product variant ID") @Valid @RequestPart("review") ReviewDTO reviewDTO,
            @Parameter(description = "Optional images for the review") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {
        reviewService.createReview(reviewDTO, images, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo đánh giá thành công"));
    }


    /**
     * Lấy danh sách reviews của user hiện tại
     */
    @GetMapping("/my-reviews")
    @Operation(summary = "Get my reviews", description = "Retrieve all reviews created by the current authenticated user")
    public ResponseEntity<?> getMyReviews(
            @AuthenticationPrincipal User currentUser) throws Exception {

        List<Review> reviews = reviewService.getReviewsByUser(currentUser.getId());
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(ReviewResponse::fromReview)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
    }

    /**
     * Cập nhật review
     */
    @PutMapping("/{reviewId}")
    @Operation(summary = "Update review", description = "Update an existing review (only by the review owner)")
    public ResponseEntity<?> updateReview(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String reviewId,
            @Parameter(description = "Updated review information") @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal User currentUser) throws Exception {

        reviewService.updateReview(reviewId, reviewDTO, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đánh giá thành công"));
    }

    /**
     * Xóa review
     */
    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review (only by the review owner)")
    public ResponseEntity<?> deleteReview(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String reviewId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        reviewService.deleteReview(reviewId, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Review deleted successfully"));
    }
}
