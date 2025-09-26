package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.CardItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardItemRepository extends JpaRepository<CardItem, String> {
    
    /**
     * Tìm tất cả items trong giỏ hàng
     */
    List<CardItem> findByCartId(String cartId);
    
    /**
     * Tìm item cụ thể trong giỏ hàng theo cart_id và product_variant_id
     */
    Optional<CardItem> findByCartIdAndProductVariantId(String cartId, String productVariantId);
    
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
    Optional<CardItem> findByIdAndCartId(String id, String cartId);
    
    /**
     * Lấy tổng số lượng sản phẩm trong giỏ hàng
     */
    @Query("SELECT SUM(ci.quantity) FROM CardItem ci WHERE ci.cartId = :cartId")
    Optional<Integer> getTotalQuantityByCartId(@Param("cartId") String cartId);
    
    /**
     * Tìm tất cả items của user thông qua cart
     */
    @Query("SELECT ci FROM CardItem ci JOIN Cart c ON ci.cartId = c.id WHERE c.userId = :userId")
    List<CardItem> findByUserId(@Param("userId") String userId);
}
