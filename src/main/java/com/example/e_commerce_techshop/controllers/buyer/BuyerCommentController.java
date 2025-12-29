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
@Tag(name = "Buyer Comment Management", description = "API cho quản lý bình luận của người mua - Xử lý tạo, cập nhật, xóa và lấy bình luận sản phẩm")
@SecurityRequirement(name = "bearerAuth")
public class BuyerCommentController {

    private final ICommentService commentService;

    @GetMapping("/my-comments")
    @Operation(summary = "Lấy bình luận của tôi", description = "Lấy tất cả các bình luận do người dùng hiện tại tạo")
    public ResponseEntity<?> getMyComments(
            @AuthenticationPrincipal User currentUser) throws Exception {

        List<Comment> comments = commentService.getCommentsByUser(currentUser.getId());
        List<CommentResponse> commentResponses = comments.stream()
                .map(CommentResponse::fromComment)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(commentResponses));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo bình luận sản phẩm", description = "ParantCommentId là tùy chọn, dùng để trả lời bình luận khác")
    public ResponseEntity<?> createComment(
            @Parameter(description = "Thông tin bình luận bao gồm nội dung và ID sản phẩm") @Valid @RequestPart("comment") CommentDTO commentDTO,
            @Parameter(description = "Ảnh tùy chọn cho bình luận") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {
        commentService.createComment(commentDTO, images, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo bình luận thành công"));
    }

    @PutMapping(value = "/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật bình luận", description = "Cập nhật một bình luận hiện có (chỉ bởi chủ bình luận)")
    public ResponseEntity<?> updateComment(
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId,
            @Parameter(description = "Thông tin cập nhật bình luận") @Valid @RequestPart("comment") UpdateCommentDTO commentDTO,
            @Parameter(description = "Ảnh tùy chọn cho bình luận") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {

        commentService.updateComment(commentId, commentDTO, images, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bình luận thành công"));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Xóa bình luận", description = "Xóa một bình luận (chỉ bởi chủ bình luận)")
    public ResponseEntity<?> deleteComment(
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        commentService.deleteComment(commentId, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Xóa bình luận thành công"));
    }
}
