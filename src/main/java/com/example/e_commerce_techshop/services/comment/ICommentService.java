package com.example.e_commerce_techshop.services.comment;

import com.example.e_commerce_techshop.dtos.CommentDTO;
import com.example.e_commerce_techshop.dtos.buyer.UpdateCommentDTO;
import com.example.e_commerce_techshop.models.Comment;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.CommentResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ICommentService {
    
    // CRUD operations for buyers
    void createComment(CommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception;
    void updateComment(String commentId, UpdateCommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception;
    void deleteComment(String commentId, User currentUser) throws Exception;
    
    // CRUD operations for stores
    void createStoreComment(String storeId, CommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception;
    void updateStoreComment(String storeId, String commentId, UpdateCommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception;
    void deleteStoreComment(String storeId, String commentId, User currentUser) throws Exception;
    
    // Get comments by product with pagination
    Page<Comment> getCommentsByProductVariant(String productVariantId, Pageable pageable);
    
    // Get comments by product variant with replies included
    Page<CommentResponse> getCommentsByProductVariantWithReplies(String productVariantId, Pageable pageable);
    
    // Get comments by user
    List<Comment> getCommentsByUser(String userId);
    
    // Get comments by store
    List<Comment> getCommentsByStore(String storeId);
    
    // Get comment by ID
    CommentResponse getCommentById(String commentId) throws Exception;
    
    // Get comment statistics
    Long getCommentCountByProductVariant(String productVariantId);
    
    // Get replies for a comment
    List<CommentResponse> getRepliesByComment(String commentId);
}
