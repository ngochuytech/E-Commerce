package com.example.e_commerce_techshop.controllers.b2c;

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
@RequestMapping("${api.prefix}/b2c/comments")
@RequiredArgsConstructor
@RequireActiveAccount
@Tag(name = "B2C Comment Management", description = "APIs for store owners to manage their comments on products")
@SecurityRequirement(name = "bearerAuth")
public class B2CCommentController {

    private final ICommentService commentService;

    /**
     * Tạo comment mới cho sản phẩm (từ phía store owner)
     */
    @PostMapping(value = "/store/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create store comment", description = "ParantCommentId là tùy chọn, dùng để trả lời bình luận khác. Chỉ cửa hàng sở hữu sản phẩm mới có thể tạo bình luận này")
    public ResponseEntity<?> createStoreComment(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String storeId,
            @Parameter(description = "Comment information including content and product variant ID") @Valid @RequestPart("comment") CommentDTO commentDTO,
            @Parameter(description = "Optional images for the comment (max 5)") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {
        commentService.createStoreComment(storeId, commentDTO, images, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tạo bình luận từ cửa hàng thành công"));
    }

    /**
     * Lấy danh sách comments của store hiện tại
     */
    @GetMapping("/store/{storeId}/my-comments")
    @Operation(summary = "Get store comments", description = "Retrieve all comments created by this store")
    public ResponseEntity<?> getStoreComments(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String storeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        List<Comment> comments = commentService.getCommentsByStore(storeId);
        List<CommentResponse> commentResponses = comments.stream()
                .map(CommentResponse::fromComment)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(commentResponses));
    }

    /**
     * Cập nhật comment của store
     */
    @PutMapping(value = "/store/{storeId}/comment/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update store comment", description = "Update an existing store comment (only by the store owner)")
    public ResponseEntity<?> updateStoreComment(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String storeId,
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId,
            @Parameter(description = "Updated comment information") @Valid @RequestPart("comment") UpdateCommentDTO commentDTO,
            @Parameter(description = "Optional images for the comment (max 5)") @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User currentUser) throws Exception {

        commentService.updateStoreComment(storeId, commentId, commentDTO, images, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Cập nhật bình luận cửa hàng thành công"));
    }

    /**
     * Xóa comment của store
     */
    @DeleteMapping("/store/{storeId}/comment/{commentId}")
    @Operation(summary = "Delete store comment", description = "Delete a store comment (only by the store owner)")
    public ResponseEntity<?> deleteStoreComment(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String storeId,
            @Parameter(description = "Comment ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String commentId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        commentService.deleteStoreComment(storeId, commentId, currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Xóa bình luận cửa hàng thành công"));
    }
}
