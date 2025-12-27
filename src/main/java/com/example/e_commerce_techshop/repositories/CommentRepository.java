package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    
    // Find comments by user
    List<Comment> findByUserId(String userId);
    
    // Find comments by store
    List<Comment> findByStoreId(String storeId);
    
    // Find comments by parent comment (for replies)
    List<Comment> findByParentCommentId(String parentCommentId);
    
    // Count comments by product variant
    Long countByProductVariantId(String productVariantId);
    
    // Find top-level comments (no parent) by product variant
    Page<Comment> findByProductVariantIdAndParentCommentIsNull(String productVariantId, Pageable pageable);
    
    // Find comments by product variant and comment type
    Page<Comment> findByProductVariantIdAndCommentTypeAndParentCommentIsNull(String productVariantId, String commentType, Pageable pageable);
}
