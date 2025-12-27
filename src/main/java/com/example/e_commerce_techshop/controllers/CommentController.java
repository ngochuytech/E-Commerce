package com.example.e_commerce_techshop.controllers;

import java.util.HashMap;
import java.util.List;
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

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.CommentResponse;
import com.example.e_commerce_techshop.services.comment.ICommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Management", description = "APIs for product comments and statistics")
public class CommentController {

    private final ICommentService commentService;

    /**
     * Lấy danh sách comments theo product variant ID
     */
    @GetMapping("/product-variant/{productVariantId}")
    @Operation(summary = "Get comments by product variant", description = "Retrieve paginated comments for a specific product variant with sorting options. Includes nested replies.")
    public ResponseEntity<?> getCommentsByProductVariant(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String productVariantId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Sử dụng method mới để lấy comments kèm replies
        Page<CommentResponse> commentResponses = commentService.getCommentsByProductVariantWithReplies(productVariantId, pageable);

        return ResponseEntity.ok(ApiResponse.ok(commentResponses));
    }

    /**
     * Lấy thông tin chi tiết một comment
     */
    @GetMapping("/{commentId}")
    @Operation(summary = "Get comment by ID", description = "Retrieve detailed information of a specific comment including all replies")
    public ResponseEntity<?> getCommentById(
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId)
            throws Exception {
        CommentResponse commentResponse = commentService.getCommentById(commentId);

        return ResponseEntity.ok(ApiResponse.ok(commentResponse));
    }

    /**
     * Lấy danh sách replies của một comment
     */
    @GetMapping("/{commentId}/replies")
    @Operation(summary = "Get replies by comment", description = "Retrieve all replies for a specific comment")
    public ResponseEntity<?> getRepliesByComment(
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId)
            throws Exception {
        List<CommentResponse> replies = commentService.getRepliesByComment(commentId);

        return ResponseEntity.ok(ApiResponse.ok(replies));
    }

    /**
     * Lấy thống kê comments cho một product variant
     */
    @GetMapping("/product-variant/{productVariantId}/statistics")
    @Operation(summary = "Get product comment statistics", description = "Get comment statistics for a product variant including total comments count")
    public ResponseEntity<?> getProductCommentStats(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String productVariantId)
            throws Exception {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalComments = commentService.getCommentCountByProductVariant(productVariantId);
        stats.put("totalComments", totalComments);
        stats.put("productVariantId", productVariantId);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
