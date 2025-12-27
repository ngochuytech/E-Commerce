package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Comment;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private String id;
    private String productVariantId;
    private String content;
    private List<String> imageUrls;
    private UserResponse user;
    private StoreResponse store;
    private String commentType;
    private String parentCommentId;
    private Boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Danh sách replies (nested comments)
    private List<CommentResponse> replies;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private String id;
        private String name;
        private String email;
        private String avatar;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StoreResponse {
        private String id;
        private String name;
        private String avatar;
    }

    public static CommentResponse fromComment(Comment comment) {
        if (comment == null) {
            return null;
        }
        
        UserResponse userResponse = null;
        if (comment.getUser() != null) {
            userResponse = UserResponse.builder()
                    .id(comment.getUser().getId())
                    .name(comment.getUser().getFullName())
                    .email(comment.getUser().getEmail())
                    .avatar(comment.getUser().getAvatar())
                    .build();
        }
        
        StoreResponse storeResponse = null;
        if (comment.getStore() != null) {
            storeResponse = StoreResponse.builder()
                    .id(comment.getStore().getId())
                    .name(comment.getStore().getName())
                    .avatar(comment.getStore().getLogoUrl())
                    .build();
        }
        
        return CommentResponse.builder()
                .id(comment.getId())
                .productVariantId(comment.getProductVariant() != null ? comment.getProductVariant().getId() : null)
                .content(comment.getContent())
                .imageUrls(comment.getImageUrls())
                .user(userResponse)
                .store(storeResponse)
                .commentType(comment.getCommentType())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .isEdited(comment.getIsEdited() != null ? comment.getIsEdited() : false)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
