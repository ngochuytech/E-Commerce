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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.annotations.RequireActiveAccount;
import com.example.e_commerce_techshop.dtos.CommentDTO;
import com.example.e_commerce_techshop.dtos.buyer.UpdateCommentDTO;
import com.example.e_commerce_techshop.models.Comment;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.CommentResponse;
import com.example.e_commerce_techshop.services.comment.ICommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/buyer/comments")
@RequiredArgsConstructor
@RequireActiveAccount
@Tag(name = "Buyer Comment Management", description = "APIs for buyers to manage their comments")
@SecurityRequirement(name = "bearerAuth")
public class BuyerCommentController {

    private final ICommentService commentService;

    /**
     * Tạo comment mới cho sản phẩm
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create product comment", description = "ParantCommentId là tùy chọn, dùng để trả lời bình luận khác")
    public ResponseEntity<?> createComment(
            @Parameter(description = "Comment information including content and product ID") @Valid @RequestPart("comment") CommentDTO commentDTO,
            @Parameter(description = "Optional images for the comment") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {
        commentService.createComment(commentDTO, images, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo bình luận thành công"));
    }

    /**
     * Lấy danh sách comments của user hiện tại
     */
    @GetMapping("/my-comments")
    @Operation(summary = "Get my comments", description = "Retrieve all comments created by the current authenticated user")
    public ResponseEntity<?> getMyComments(
            @AuthenticationPrincipal User currentUser) throws Exception {

        List<Comment> comments = commentService.getCommentsByUser(currentUser.getId());
        List<CommentResponse> commentResponses = comments.stream()
                .map(CommentResponse::fromComment)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(commentResponses));
    }

    /**
     * Cập nhật comment
     */
    @PutMapping(value = "/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update comment", description = "Update an existing comment (only by the comment owner)")
    public ResponseEntity<?> updateComment(
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId,
            @Parameter(description = "Updated comment information") @Valid @RequestPart("comment") UpdateCommentDTO commentDTO,
            @Parameter(description = "Optional images for the comment") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {

        commentService.updateComment(commentId, commentDTO, images, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bình luận thành công"));
    }

    /**
     * Xóa comment
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete comment", description = "Delete a comment (only by the comment owner)")
    public ResponseEntity<?> deleteComment(
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        commentService.deleteComment(commentId, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Xóa bình luận thành công"));
    }
}
