package com.example.e_commerce_techshop.services.promotion;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.exceptions.InvalidPromotionException;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.PromotionResponse;

import java.util.Map;

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
    
    // Promotion Validation & Application
    boolean validatePromotion(String promotionId, Long orderValue) throws Exception;
    boolean validatePromotionForUser(String promotionId, Long orderValue, User user) throws Exception;
    void validatePromotionForUser(Promotion promotion, Long orderValue, User user) throws InvalidPromotionException;
    void recordPromotionUsage(Promotion promotion, User user) throws Exception;
    Long calculateDiscount(String promotionId, Long orderValue) throws Exception;
    
    // Promotion Reports
        // Admin
    Page<PromotionResponse> getExpiredPlatformPromotions(Pageable pageable);
    Page<PromotionResponse> getDeletedPlatformPromotions(Pageable pageable);
    Page<PromotionResponse> getInactivePlatformPromotions(Pageable pageable);
    Page<PromotionResponse> getActivePlatformPromotions(Pageable pageable);
        // Store Owner
    Map<String, Long> countPromotionsByStatus(String storeId) throws Exception;
    Page<PromotionResponse> getPromotionsByStore(String storeId, Pageable pageable);
    Page<PromotionResponse> getPromotionsByStoreAndStatus(String storeId, String status, Pageable pageable);
    Page<PromotionResponse> getExpiredPromotionsByStore(String storeId, Pageable pageable);
        // General
    Promotion getPromotionById(String promotionId) throws Exception;
    Promotion getPromotionByCode(String promotionCode) throws Exception;
    Page<PromotionResponse> getPromotionsByType(String type, Pageable pageable);
    Page<Promotion> getAllPromotions(Pageable pageable);
    Page<Promotion> getAllPromotionForCustomer(Pageable pageable);
    Page<PromotionResponse> getPlatformPromotionsForCustomer(Long orderValue, User user,Pageable pageable);
    Page<PromotionResponse> getStorePromotionsForCustomer(String storeId, Long orderValue, User user, Pageable pageable);
}
