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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.annotations.RequireActiveAccount;
import com.example.e_commerce_techshop.dtos.ReviewDTO;
import com.example.e_commerce_techshop.dtos.buyer.UpdateReviewDTO;
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
@Tag(name = "Buyer Review Management", description = "API cho quản lý đánh giá của người mua - Xử lý tạo, cập nhật, xóa và lấy đánh giá sản phẩm")
@SecurityRequirement(name = "bearerAuth")
public class BuyerReviewController {

    private final IReviewService reviewService;

    @GetMapping("/my-reviews")
    @Operation(summary = "Lấy đánh giá của tôi", description = "Lấy tất cả các đánh giá được tạo bởi người dùng hiện tại đã xác thực")
    public ResponseEntity<?> getMyReviews(
            @AuthenticationPrincipal User currentUser) throws Exception {

        List<Review> reviews = reviewService.getReviewsByUser(currentUser.getId());
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(ReviewResponse::fromReview)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(reviewResponses));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo đánh giá sản phẩm", description = "Tạo một đánh giá mới cho sản phẩm với đánh giá và bình luận")
    public ResponseEntity<?> createReview(
            @Parameter(description = "Thông tin đánh giá bao gồm điểm đánh giá, bình luận và ID biến thể sản phẩm") @Valid @RequestPart("review") ReviewDTO reviewDTO,
            @Parameter(description = "Hình ảnh tùy chọn cho đánh giá") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {
        reviewService.createReview(reviewDTO, images, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo đánh giá thành công"));
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật đánh giá", description = "Cập nhật một đánh giá hiện có (chỉ bởi chủ sở hữu đánh giá)")
    public ResponseEntity<?> updateReview(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String reviewId,
            @Parameter(description = "Thông tin đánh giá cập nhật") @Valid @RequestPart("review") UpdateReviewDTO reviewDTO,
            @Parameter(description = "Hình ảnh tùy chọn cho đánh giá") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {

        reviewService.updateReview(reviewId, reviewDTO, images, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật đánh giá thành công"));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Xóa đánh giá", description = "Xóa một đánh giá (chỉ bởi chủ sở hữu đánh giá)")
    public ResponseEntity<?> deleteReview(
            @Parameter(description = "Review ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String reviewId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        reviewService.deleteReview(reviewId, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Review deleted successfully"));
    }
}
