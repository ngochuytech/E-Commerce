package com.example.e_commerce_techshop.services.promotion;

import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.responses.PromotionResponse;

import java.util.List;

public interface IPromotionService {
    // Promotion Management
    PromotionResponse createPromotion(PromotionDTO promotionDTO) throws Exception;
    PromotionResponse updatePromotion(String promotionId, PromotionDTO promotionDTO) throws Exception;
    PromotionResponse getPromotionById(String promotionId) throws Exception;
    List<PromotionResponse> getAllPromotions();
    List<PromotionResponse> getPromotionsByStore(String storeId);
    void deletePromotion(String promotionId) throws Exception;
    
    // Promotion Status Management
    PromotionResponse activatePromotion(String promotionId) throws Exception;
    PromotionResponse deactivatePromotion(String promotionId) throws Exception;
    List<PromotionResponse> getActivePromotions();
    List<PromotionResponse> getActivePromotionsByStore(String storeId);
    
    // Promotion Validation & Application
    PromotionResponse validatePromotion(String promotionId, Long orderValue) throws Exception;
    Long calculateDiscount(String promotionId, Long orderValue) throws Exception;
    
    // Promotion Reports
    List<PromotionResponse> getExpiredPromotions();
    List<PromotionResponse> getPromotionsByType(String type);
}
