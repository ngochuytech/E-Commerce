package com.example.e_commerce_techshop.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment extends BaseEntity {
    @Id
    private String id;

    private String content;

    private List<String> imageUrls;

    @DBRef
    private ProductVariant productVariant;

    @DBRef
    private User user;
    
    @DBRef
    private Store store;
    
    // Comment type: BUYER or STORE
    private String commentType; // "BUYER" or "STORE"
    
    // Parent comment for reply functionality (optional for future)
    @DBRef
    private Comment parentComment;
    
    private Boolean isEdited;
    
    public enum CommentType {
        BUYER, STORE
    }
}
