package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    
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
    @Query("DELETE FROM CartItem ci WHERE ci.id = :itemId AND ci.cart.id = :cartId")
    @Modifying
    void deleteByIdAndCartId(@Param("itemId") String itemId, @Param("cartId") String cartId);
    
    /**
     * Tìm tất cả items của user thông qua cart
     */
    @Query("SELECT ci FROM CartItem ci JOIN Cart c ON ci.cart.id = c.id WHERE c.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") String userId);
}
