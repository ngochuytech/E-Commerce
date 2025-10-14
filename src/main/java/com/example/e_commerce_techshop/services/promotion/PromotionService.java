package com.example.e_commerce_techshop.services.promotion;

import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.repositories.PromotionRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService implements IPromotionService {
    
    private final PromotionRepository promotionRepository;
    private final StoreRepository storeRepository;

    @Override
    public PromotionResponse createPromotion(PromotionDTO promotionDTO) throws Exception {
        Store store = storeRepository.findById(promotionDTO.getStoreId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));

        // Validate dates
        if (promotionDTO.getStartDate().isAfter(promotionDTO.getEndDate())) {
            throw new Exception("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        Promotion promotion = Promotion.builder()
                .title(promotionDTO.getTitle())
                .store(store)
                .type(promotionDTO.getType())
                .discountType(promotionDTO.getDiscountType())
                .discountValue(promotionDTO.getDiscountValue())
                .startDate(promotionDTO.getStartDate())
                .endDate(promotionDTO.getEndDate())
                .minOrderValue(promotionDTO.getMinOrderValue() != null ? promotionDTO.getMinOrderValue() : 0L)
                .maxDiscountValue(promotionDTO.getMaxDiscountValue() != null ? promotionDTO.getMaxDiscountValue() : 0L)
                .status(promotionDTO.getStatus() != null ? promotionDTO.getStatus() : "ACTIVE")
                .build();

        Promotion savedPromotion = promotionRepository.save(promotion);
        return PromotionResponse.fromPromotion(savedPromotion);
    }

    @Override
    public PromotionResponse updatePromotion(String promotionId, PromotionDTO promotionDTO) throws Exception {
        Promotion existingPromotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Update fields
        existingPromotion.setTitle(promotionDTO.getTitle());
        existingPromotion.setType(promotionDTO.getType());
        existingPromotion.setDiscountType(promotionDTO.getDiscountType());
        existingPromotion.setDiscountValue(promotionDTO.getDiscountValue());
        existingPromotion.setStartDate(promotionDTO.getStartDate());
        existingPromotion.setEndDate(promotionDTO.getEndDate());
        existingPromotion.setMinOrderValue(promotionDTO.getMinOrderValue());
        existingPromotion.setMaxDiscountValue(promotionDTO.getMaxDiscountValue());
        existingPromotion.setStatus(promotionDTO.getStatus());

        // Update store if provided
        if (promotionDTO.getStoreId() != null) {
            Store store = storeRepository.findById(promotionDTO.getStoreId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng"));
            existingPromotion.setStore(store);
        }

        Promotion updatedPromotion = promotionRepository.save(existingPromotion);
        return PromotionResponse.fromPromotion(updatedPromotion);
    }

    @Override
    public PromotionResponse getPromotionById(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
        return PromotionResponse.fromPromotion(promotion);
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();
        return promotions.stream().map(PromotionResponse::fromPromotion).toList();
    }

    @Override
    public List<PromotionResponse> getPromotionsByStore(String storeId) {
        List<Promotion> promotions = promotionRepository.findByStoreId(storeId);
        return promotions.stream().map(PromotionResponse::fromPromotion).toList();
    }

    @Override
    public void deletePromotion(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
        promotionRepository.delete(promotion);
    }

    @Override
    public PromotionResponse activatePromotion(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
        
        promotion.setStatus("ACTIVE");
        Promotion updatedPromotion = promotionRepository.save(promotion);
        return PromotionResponse.fromPromotion(updatedPromotion);
    }

    @Override
    public PromotionResponse deactivatePromotion(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
        
        promotion.setStatus("INACTIVE");
        Promotion updatedPromotion = promotionRepository.save(promotion);
        return PromotionResponse.fromPromotion(updatedPromotion);
    }

    @Override
    public List<PromotionResponse> getActivePromotions() {
        List<Promotion> promotions = promotionRepository.findActivePromotions(LocalDateTime.now());
        return promotions.stream().map(PromotionResponse::fromPromotion).toList();
    }

    @Override
    public List<PromotionResponse> getActivePromotionsByStore(String storeId) {
        List<Promotion> promotions = promotionRepository.findActivePromotionsByStore(storeId, LocalDateTime.now());
        return promotions.stream().map(PromotionResponse::fromPromotion).toList();
    }

    @Override
    public PromotionResponse validatePromotion(String promotionId, Long orderValue) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        LocalDateTime now = LocalDateTime.now();
        
        // Check if promotion is active and not expired
        if (!"ACTIVE".equals(promotion.getStatus()) || 
            promotion.getStartDate().isAfter(now) || 
            promotion.getEndDate().isBefore(now)) {
            throw new Exception("Khuyến mãi không còn hiệu lực");
        }

        // Check minimum order value
        if (orderValue < promotion.getMinOrderValue()) {
            throw new Exception("Đơn hàng không đạt giá trị tối thiểu cho khuyến mãi");
        }

        return PromotionResponse.fromPromotion(promotion);
    }

    @Override
    public Long calculateDiscount(String promotionId, Long orderValue) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        Long discount = 0L;

        if ("PERCENTAGE".equals(promotion.getType())) {
            discount = (orderValue * promotion.getDiscountValue()) / 100;
        } else if ("FIXED_AMOUNT".equals(promotion.getType())) {
            discount = promotion.getDiscountValue();
        }

        // Apply maximum discount limit
        if (promotion.getMaxDiscountValue() > 0 && discount > promotion.getMaxDiscountValue()) {
            discount = promotion.getMaxDiscountValue();
        }

        return discount;
    }

    @Override
    public List<PromotionResponse> getExpiredPromotions() {
        List<Promotion> promotions = promotionRepository.findExpiredPromotions(LocalDateTime.now());
        return promotions.stream().map(PromotionResponse::fromPromotion).toList();
    }

    @Override
    public List<PromotionResponse> getPromotionsByType(String type) {
        List<Promotion> promotions = promotionRepository.findByType(type);
        return promotions.stream().map(PromotionResponse::fromPromotion).toList();
    }
}
