package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    
    /**
     * Tìm giỏ hàng theo user ID
     */
    Optional<Cart> findByUserId(String userId);
    
    /**
     * Kiểm tra user đã có giỏ hàng chưa
     */
    boolean existsByUserId(String userId);
    
    /**
     * Xóa giỏ hàng của user
     */
    void deleteByUserId(String userId);
    
    /**
     * Tìm giỏ hàng theo ID và user ID (đảm bảo user chỉ truy cập giỏ hàng của mình)
     */
    Optional<Cart> findByIdAndUserId(String id, String userId);
}
