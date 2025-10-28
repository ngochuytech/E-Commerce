package com.example.e_commerce_techshop.services.promotion;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.responses.PromotionResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPromotionService {

    // Promotion Management
        // Admin
    void createPlatformPromotion(CreatePromotionDTO createPromotionDTO) throws Exception;
    void updatePlatformPromotion(String promotionId, PromotionDTO promotionDTO) throws Exception;
    void activatePromotion(String promotionId) throws Exception;
    void deactivatePromotion(String promotionId) throws Exception;
    void deletePromotion(String promotionId) throws Exception;

        // Store Owner
    void createStorePromotion(CreatePromotionDTO createPromotionDTO, String storeId) throws Exception;
    void updateStorePromotion(String promotionId, PromotionDTO promotionDTO, String ownerId) throws Exception;
    void activateStorePromotion(String promotionId, String ownerId) throws Exception;
    void deactivateStorePromotion(String promotionId, String ownerId) throws Exception;
    void deleteStorePromotion(String promotionId, String ownerId) throws Exception;
        // General
    Promotion getPromotionById(String promotionId) throws Exception;
    Page<Promotion> getAllPromotions(Pageable pageable);
    Page<Promotion> getPlatformPromotions(Pageable pageable);
    Page<PromotionResponse> getPromotionsByStore(String storeId, Pageable pageable);
    
    // Promotion Status Management
    Page<PromotionResponse> getActivePromotions(Pageable pageable);
    Page<PromotionResponse> getActivePromotionsByStore(String storeId, Pageable pageable);
    
    // Promotion Validation & Application
    PromotionResponse validatePromotion(String promotionId, Long orderValue) throws Exception;
    Long calculateDiscount(String promotionId, Long orderValue) throws Exception;
    
    // Promotion Reports
        // Admin
    Page<PromotionResponse> getExpiredPlatformPromotions(Pageable pageable);
    Page<PromotionResponse> getDeletedPlatformPromotions(Pageable pageable);
    Page<PromotionResponse> getInactivePlatformPromotions(Pageable pageable);
    Page<PromotionResponse> getActivePlatformPromotions(Pageable pageable);
        // General
    Page<PromotionResponse> getExpiredPromotions(Pageable pageable);
    Page<PromotionResponse> getPromotionsByType(String type, Pageable pageable);
    Page<PromotionResponse> getDeletedPromotions(Pageable pageable);
}
