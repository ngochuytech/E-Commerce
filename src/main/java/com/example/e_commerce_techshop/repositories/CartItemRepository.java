package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.CartItem;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends MongoRepository<CartItem, String> {
    
    /**
     * Tìm tất cả items trong giỏ hàng
     */
    List<CartItem> findByCartId(String cartId);
    
    /**
     * Tìm item cụ thể trong giỏ hàng theo cart_id và product_variant_id
     */
    Optional<CartItem> findByCartIdAndProductVariantId(String cartId, String productVariantId);
    
    /**
     * Kiểm tra item đã tồn tại trong giỏ hàng chưa
     */
    boolean existsByCartIdAndProductVariantId(String cartId, String productVariantId);
    
    /**
     * Đếm số lượng items trong giỏ hàng
     */
    long countByCartId(String cartId);
    
    /**
     * Xóa tất cả items trong giỏ hàng
     */
    void deleteByCartId(String cartId);
    
    /**
     * Xóa item cụ thể khỏi giỏ hàng
     */
    void deleteByCartIdAndProductVariantId(String cartId, String productVariantId);
    
    /**
     * Tìm item theo ID và cart ID (đảm bảo security)
     */
    Optional<CartItem> findByIdAndCartId(String id, String cartId);
    
    /**
     * Xóa item theo ID và cart ID (sử dụng custom query)
     */
    @Query(value = "{ 'id': ?0, 'cart.$id': ?1 }", delete = true)
    void deleteByIdAndCartId(String itemId, String cartId);
    
    /**
     * Tìm tất cả items của user thông qua cart
     */
    @Query("{ 'cart.user.$id': ?0 }")
    List<CartItem> findByUserId(String userId);
}
