package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
    List<Promotion> findByStoreId(String storeId);
    List<Promotion> findByStatus(String status);
    List<Promotion> findByType(String type);
    
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Promotion p WHERE p.store.id = :storeId AND p.status = 'ACTIVE' AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotionsByStore(@Param("storeId") String storeId, @Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Promotion p WHERE p.endDate < :now")
    List<Promotion> findExpiredPromotions(@Param("now") LocalDateTime now);

    Optional<Promotion> findByIdAndStoreId(String id, String storeId);
}



