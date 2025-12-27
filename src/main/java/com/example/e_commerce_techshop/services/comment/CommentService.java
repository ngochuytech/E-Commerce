package com.example.e_commerce_techshop.services.comment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.CommentDTO;
import com.example.e_commerce_techshop.dtos.buyer.UpdateCommentDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Comment;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.CommentRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.responses.CommentResponse;
import com.example.e_commerce_techshop.services.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService implements ICommentService {

    private final CommentRepository commentRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public void createComment(CommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception {
        // Kiểm tra product tồn tại
        ProductVariant variant = productVariantRepository.findById(commentDTO.getProductVariantId())
                .orElseThrow(() -> new DataNotFoundException("Product not found with id: " + commentDTO.getProductVariantId()));

        // Upload images nếu có
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            if (images.size() > 5) {
                throw new IllegalArgumentException("Maximum 5 images allowed");
            }
            imageUrls = cloudinaryService.uploadMediaFiles(images, "comments");
        }

        // Xử lý parent comment nếu là reply
        Comment parentComment = null;
        if (commentDTO.getParentCommentId() != null && !commentDTO.getParentCommentId().isEmpty()) {
            parentComment = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElseThrow(() -> new DataNotFoundException("Parent comment not found"));
        }

        // Tạo comment
        Comment comment = Comment.builder()
                .content(commentDTO.getContent())
                .imageUrls(imageUrls)
                .productVariant(variant)
                .user(currentUser)
                .commentType(Comment.CommentType.BUYER.name())
                .parentComment(parentComment)
                .isEdited(false)
                .build();

        commentRepository.save(comment);
        log.info("User {} created comment for product {}", currentUser.getId(), variant.getId());
    }

    @Override
    @Transactional
    public void updateComment(String commentId, UpdateCommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("Comment not found with id: " + commentId));

        // Kiểm tra quyền sở hữu
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You are not authorized to update this comment");
        }

        comment.setContent(commentDTO.getContent());
        comment.setIsEdited(true);

        // Upload images mới nếu có
        if (images != null && !images.isEmpty()) {
            if (images.size() > 5) {
                throw new IllegalArgumentException("Maximum 5 images allowed");
            }
            
            // Xóa ảnh cũ trên Cloudinary
            if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
                for (String imageUrl : comment.getImageUrls()) {
                    try {
                        cloudinaryService.deleteImageByUrl(imageUrl);
                    } catch (Exception e) {
                        log.warn("Failed to delete old image: {}", imageUrl);
                    }
                }
            }
            
            // Upload ảnh mới
            List<String> newImageUrls = cloudinaryService.uploadMediaFiles(images, "comments");
            comment.setImageUrls(newImageUrls);
        }

        commentRepository.save(comment);
        log.info("User {} updated comment {}", currentUser.getId(), commentId);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, User currentUser) throws Exception {
        // Kiểm tra comment tồn tại
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("Comment not found with id: " + commentId));

        // Kiểm tra quyền sở hữu
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You are not authorized to delete this comment");
        }

        // Xóa ảnh trên Cloudinary
        if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
            for (String imageUrl : comment.getImageUrls()) {
                try {
                    cloudinaryService.deleteImageByUrl(imageUrl);
                } catch (Exception e) {
                    log.warn("Failed to delete image: {}", imageUrl);
                }
            }
        }

        // Xóa các replies nếu có
        List<Comment> replies = commentRepository.findByParentCommentId(commentId);
        if (!replies.isEmpty()) {
            for (Comment reply : replies) {
                // Xóa ảnh của replies
                if (reply.getImageUrls() != null && !reply.getImageUrls().isEmpty()) {
                    for (String imageUrl : reply.getImageUrls()) {
                        try {
                            cloudinaryService.deleteImageByUrl(imageUrl);
                        } catch (Exception e) {
                            log.warn("Failed to delete reply image: {}", imageUrl);
                        }
                    }
                }
            }
            commentRepository.deleteAll(replies);
        }

        commentRepository.delete(comment);
        log.info("User {} deleted comment {}", currentUser.getId(), commentId);
    }

    @Override
    public Page<Comment> getCommentsByProductVariant(String productVariantId, Pageable pageable) {
        return commentRepository.findByProductVariantIdAndParentCommentIsNull(productVariantId, pageable);
    }
    
    @Override
    public Page<CommentResponse> getCommentsByProductVariantWithReplies(String productVariantId, Pageable pageable) {
        // Lấy các top-level comments
        Page<Comment> topLevelComments = commentRepository.findByProductVariantIdAndParentCommentIsNull(productVariantId, pageable);
        
        // Convert sang CommentResponse và load replies cho mỗi comment
        return topLevelComments.map(comment -> {
            CommentResponse response = CommentResponse.fromComment(comment);
            
            // Load replies cho comment này
            List<Comment> replies = commentRepository.findByParentCommentId(comment.getId());
            if (!replies.isEmpty()) {
                List<CommentResponse> replyResponses = replies.stream()
                        .map(CommentResponse::fromComment)
                        .collect(Collectors.toList());
                response.setReplies(replyResponses);
            }
            
            return response;
        });
    }

    @Override
    public List<Comment> getCommentsByUser(String userId) {
        return commentRepository.findByUserId(userId);
    }

    @Override
    public CommentResponse getCommentById(String commentId) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("Comment not found with id: " + commentId));
        
        CommentResponse response = CommentResponse.fromComment(comment);
        
        // Load replies cho comment này
        List<Comment> replies = commentRepository.findByParentCommentId(commentId);
        if (!replies.isEmpty()) {
            List<CommentResponse> replyResponses = replies.stream()
                    .map(CommentResponse::fromComment)
                    .collect(Collectors.toList());
            response.setReplies(replyResponses);
        }
        
        return response;
    }

    @Override
    public Long getCommentCountByProductVariant(String productVariantId) {
        return commentRepository.countByProductVariantId(productVariantId);
    }

    @Override
    public List<CommentResponse> getRepliesByComment(String commentId) {
        List<Comment> replies = commentRepository.findByParentCommentId(commentId);
        return replies.stream()
                .map(CommentResponse::fromComment)
                .collect(Collectors.toList());
    }
    
    // ==================== STORE COMMENT METHODS ====================
    
    @Override
    @Transactional
    public void createStoreComment(String storeId, CommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception {
        // Kiểm tra store tồn tại
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Store not found with id: " + storeId));

        // Kiểm tra user có phải owner của store không
        if (!store.getOwner().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Bạn không phải chủ sở hữu cửa hàng này");
        }

        // Kiểm tra product variant tồn tại
        ProductVariant variant = productVariantRepository.findById(commentDTO.getProductVariantId())
                .orElseThrow(() -> new DataNotFoundException("Product variant not found with id: " + commentDTO.getProductVariantId()));

        if(!variant.getProduct().getStore().getId().equals(storeId)) {
            throw new IllegalStateException("Sản phẩm không thuộc cửa hàng của bạn");
        }

        // Upload images nếu có
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            if (images.size() > 5) {
                throw new IllegalArgumentException("Tối đa 5 hình ảnh được phép tải lên");
            }
            imageUrls = cloudinaryService.uploadMediaFiles(images, "comments");
        }

        // Xử lý parent comment nếu là reply
        Comment parentComment = null;
        if (commentDTO.getParentCommentId() != null && !commentDTO.getParentCommentId().isEmpty()) {
            parentComment = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElseThrow(() -> new DataNotFoundException("Parent comment not found"));
        }

        // Tạo store comment
        Comment comment = Comment.builder()
                .content(commentDTO.getContent())
                .imageUrls(imageUrls)
                .productVariant(variant)
                .user(currentUser)
                .store(store)
                .commentType(Comment.CommentType.STORE.name())
                .parentComment(parentComment)
                .isEdited(false)
                .build();

        commentRepository.save(comment);
        log.info("Store {} created comment for product variant {}", storeId, variant.getId());
    }

    @Override
    @Transactional
    public void updateStoreComment(String storeId, String commentId, UpdateCommentDTO commentDTO, List<MultipartFile> images, User currentUser) throws Exception {
        // Kiểm tra comment tồn tại
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("Comment not found with id: " + commentId));

        // Kiểm tra comment thuộc về store
        if (comment.getStore() == null || !comment.getStore().getId().equals(storeId)) {
            throw new IllegalStateException("This comment does not belong to this store");
        }

        // Kiểm tra quyền sở hữu
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You are not authorized to update this comment");
        }

        // Cập nhật content
        comment.setContent(commentDTO.getContent());
        comment.setIsEdited(true);

        // Upload images mới nếu có
        if (images != null && !images.isEmpty()) {
            if (images.size() > 5) {
                throw new IllegalArgumentException("Maximum 5 images allowed");
            }
            
            // Xóa ảnh cũ trên Cloudinary
            if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
                for (String imageUrl : comment.getImageUrls()) {
                    try {
                        cloudinaryService.deleteImageByUrl(imageUrl);
                    } catch (Exception e) {
                        log.warn("Failed to delete old image: {}", imageUrl);
                    }
                }
            }
            
            // Upload ảnh mới
            List<String> newImageUrls = cloudinaryService.uploadMediaFiles(images, "comments");
            comment.setImageUrls(newImageUrls);
        }

        commentRepository.save(comment);
        log.info("Store {} updated comment {}", storeId, commentId);
    }

    @Override
    @Transactional
    public void deleteStoreComment(String storeId, String commentId, User currentUser) throws Exception {
        // Kiểm tra comment tồn tại
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("Comment not found with id: " + commentId));

        // Kiểm tra comment thuộc về store
        if (comment.getStore() == null || !comment.getStore().getId().equals(storeId)) {
            throw new IllegalStateException("This comment does not belong to this store");
        }

        // Kiểm tra quyền sở hữu
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You are not authorized to delete this comment");
        }

        // Xóa ảnh trên Cloudinary
        if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
            for (String imageUrl : comment.getImageUrls()) {
                try {
                    cloudinaryService.deleteImageByUrl(imageUrl);
                } catch (Exception e) {
                    log.warn("Failed to delete image: {}", imageUrl);
                }
            }
        }

        // Xóa các replies nếu có
        List<Comment> replies = commentRepository.findByParentCommentId(commentId);
        if (!replies.isEmpty()) {
            for (Comment reply : replies) {
                // Xóa ảnh của replies
                if (reply.getImageUrls() != null && !reply.getImageUrls().isEmpty()) {
                    for (String imageUrl : reply.getImageUrls()) {
                        try {
                            cloudinaryService.deleteImageByUrl(imageUrl);
                        } catch (Exception e) {
                            log.warn("Failed to delete reply image: {}", imageUrl);
                        }
                    }
                }
            }
            commentRepository.deleteAll(replies);
        }

        commentRepository.delete(comment);
        log.info("Store {} deleted comment {}", storeId, commentId);
    }

    @Override
    public List<Comment> getCommentsByStore(String storeId) {
        return commentRepository.findByStoreId(storeId);
    }
}
