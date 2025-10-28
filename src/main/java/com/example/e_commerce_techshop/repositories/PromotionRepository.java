package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Promotion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Override findAll to exclude DELETED promotions
    @Query("{ 'status': { '$ne': 'DELETED' } }")
    Page<Promotion> findAll(Pageable pageable);

    @Query("{ 'store.$id': ObjectId(?0), 'status': { '$ne': 'DELETED' } }")
    Page<Promotion> findByStoreId(String storeId, Pageable pageable);
    
    @Query("{ 'type': ?0, 'status': { '$ne': 'DELETED' } }")
    Page<Promotion> findByType(String type, Pageable pageable);

    @Query("{ 'status': 'ACTIVE', 'startDate': { '$lte': ?0 }, 'endDate': { '$gte': ?0 } }")
    Page<Promotion> findActivePromotions(LocalDateTime now, Pageable pageable);

    @Query("{ 'store.$id': ObjectId(?0), 'status': 'ACTIVE', 'startDate': { '$lte': ?1 }, 'endDate': { '$gte': ?1 } }")
    Page<Promotion> findActivePromotionsByStore(String storeId, LocalDateTime now, Pageable pageable);

    @Query("{ 'endDate': { '$lt': ?0 } }")
    Page<Promotion> findExpiredPromotions(LocalDateTime now, Pageable pageable);

    @Query("{ 'issuer': 'PLATFORM', 'endDate': { '$lt': ?0 } }")
    Page<Promotion> findExpiredPlatformPromotions(LocalDateTime now, Pageable pageable);

    Optional<Promotion> findByIdAndStoreId(String id, String storeId);

    Optional<Promotion> findByCode(String code);
    
    boolean existsByCode(String code);

    @Query("{ 'issuer': ?0, 'status': { '$ne': 'DELETED' } }")
    Page<Promotion> findByIssuer(String issuer, Pageable pageable);
    
    // Admin 
    @Query("{ 'issuer': 'PLATFORM', 'status': 'DELETED' }")
    Page<Promotion> findDeletedPlatformPromotions(Pageable pageable);

    @Query("{ 'issuer': 'PLATFORM', 'status': 'INACTIVE' }")
    Page<Promotion> findInactivePlatformPromotions(Pageable pageable);

    @Query("{ 'issuer': 'PLATFORM', 'status': 'ACTIVE' }")
    Page<Promotion> findActivePlatformPromotions(Pageable pageable);

    // Admin only - include deleted promotions (all)
    @Query("{ 'status': 'DELETED' }")
    Page<Promotion> findDeletedPromotions(Pageable pageable);
}



