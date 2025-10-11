package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Promotion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends MongoRepository<Promotion, String> {
    List<Promotion> findByStoreId(String storeId);
    List<Promotion> findByStatus(String status);
    List<Promotion> findByType(String type);
    
    @Query("{ 'status': 'ACTIVE', 'startDate': { '$lte': ?0 }, 'endDate': { '$gte': ?0 } }")
    List<Promotion> findActivePromotions(LocalDateTime now);
    
    @Query("{ 'store.$id': ?0, 'status': 'ACTIVE', 'startDate': { '$lte': ?1 }, 'endDate': { '$gte': ?1 } }")
    List<Promotion> findActivePromotionsByStore(String storeId, LocalDateTime now);
    
    @Query("{ 'endDate': { '$lt': ?0 } }")
    List<Promotion> findExpiredPromotions(LocalDateTime now);

    Optional<Promotion> findByIdAndStoreId(String id, String storeId);
    
    /**
     * Tìm promotion theo code
     */
    Optional<Promotion> findByCode(String code);
}



