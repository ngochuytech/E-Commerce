package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.PromotionUsage;
import com.example.e_commerce_techshop.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionUsageRepository extends MongoRepository<PromotionUsage, String> {
    
    // Kiểm tra xem user đã sử dụng promotion này chưa
    boolean existsByPromotionAndUser(Promotion promotion, User user);
    
    // Đếm số lần user đã sử dụng promotion này
    int countByPromotionAndUser(Promotion promotion, User user);
    
    // Lấy lịch sử sử dụng của user với promotion cụ thể
    List<PromotionUsage> findByPromotionAndUser(Promotion promotion, User user);
    
    // Lấy tất cả lịch sử sử dụng của một promotion
    List<PromotionUsage> findByPromotion(Promotion promotion);
    
    // Lấy tất cả lịch sử sử dụng của một user
    List<PromotionUsage> findByUser(User user);
}
