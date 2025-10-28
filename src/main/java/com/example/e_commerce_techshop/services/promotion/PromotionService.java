package com.example.e_commerce_techshop.services.promotion;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.repositories.PromotionRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromotionService implements IPromotionService {
    
    private final PromotionRepository promotionRepository;
    private final StoreRepository storeRepository;

    /**
     * Tạo promotion cho Store - tự động gán issuer = STORE và storeId
     */
    @Override
    public void createStorePromotion(CreatePromotionDTO createPromotionDTO, String storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng với ID: " + storeId));

        if (createPromotionDTO.getStartDate().isAfter(createPromotionDTO.getEndDate())) {
            throw new Exception("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        if (promotionRepository.existsByCode(createPromotionDTO.getCode())) {
            throw new Exception("Mã khuyến mãi đã tồn tại: " + createPromotionDTO.getCode());
        }

        if ("FIXED_AMOUNT".equals(createPromotionDTO.getType()) && 
            createPromotionDTO.getMaxDiscountValue() != null && 
            createPromotionDTO.getMaxDiscountValue() > 0) {
            throw new Exception("Khuyến mãi FIXED_AMOUNT không được có maxDiscountValue");
        }

        Promotion promotion = Promotion.builder()
                .title(createPromotionDTO.getTitle())
                .code(createPromotionDTO.getCode())
                .store(store)
                .issuer(Promotion.Issuer.STORE.name())
                .applicableFor(createPromotionDTO.getApplicableFor())
                .type(createPromotionDTO.getType())
                .discountType(createPromotionDTO.getDiscountType())
                .discountValue(createPromotionDTO.getDiscountValue())
                .startDate(createPromotionDTO.getStartDate())
                .endDate(createPromotionDTO.getEndDate())
                .minOrderValue(createPromotionDTO.getMinOrderValue()) 
                .maxDiscountValue(createPromotionDTO.getMaxDiscountValue())
                .usageLimit(createPromotionDTO.getUsageLimit())
                .usedCount(0)
                .categoryId(createPromotionDTO.getCategoryId())
                .status(Promotion.PromotionStatus.INACTIVE.name())
                .build();

        promotionRepository.save(promotion);
    }

    /**
     * Tạo promotion cho Platform - tự động gán issuer = PLATFORM và storeId = null
     */
    @Override
    public void createPlatformPromotion(CreatePromotionDTO createPromotionDTO) throws Exception {
        if (createPromotionDTO.getStartDate().isAfter(createPromotionDTO.getEndDate())) {
            throw new Exception("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        if (promotionRepository.existsByCode(createPromotionDTO.getCode())) {
            throw new Exception("Mã khuyến mãi đã tồn tại: " + createPromotionDTO.getCode());
        }

        if ("FIXED_AMOUNT".equals(createPromotionDTO.getType()) && 
            createPromotionDTO.getMaxDiscountValue() != null && 
            createPromotionDTO.getMaxDiscountValue() > 0) {
            throw new Exception("Khuyến mãi FIXED_AMOUNT không được có maxDiscountValue");
        }

        Promotion promotion = Promotion.builder()
                .title(createPromotionDTO.getTitle())
                .code(createPromotionDTO.getCode())
                .store(null)
                .issuer(Promotion.Issuer.PLATFORM.name())
                .applicableFor(createPromotionDTO.getApplicableFor())
                .type(createPromotionDTO.getType())
                .discountType(createPromotionDTO.getDiscountType())
                .discountValue(createPromotionDTO.getDiscountValue())
                .startDate(createPromotionDTO.getStartDate())
                .endDate(createPromotionDTO.getEndDate())
                .minOrderValue(createPromotionDTO.getMinOrderValue())
                .maxDiscountValue(createPromotionDTO.getMaxDiscountValue())
                .usageLimit(createPromotionDTO.getUsageLimit())
                .usedCount(0)
                .categoryId(createPromotionDTO.getCategoryId())
                .status(Promotion.PromotionStatus.INACTIVE.name())
                .build();

        promotionRepository.save(promotion);
    }

    @Override
    public void updatePlatformPromotion(String promotionId, PromotionDTO promotionDTO) throws Exception {
        Promotion existingPromotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Validate maxDiscountValue based on type
        if ("FIXED_AMOUNT".equals(promotionDTO.getType()) && 
            promotionDTO.getMaxDiscountValue() != null && 
            promotionDTO.getMaxDiscountValue() > 0) {
            throw new Exception("Khuyến mãi FIXED_AMOUNT không được có maxDiscountValue");
        }

        // Update fields
        existingPromotion.setTitle(promotionDTO.getTitle());
        existingPromotion.setCode(promotionDTO.getCode());
        existingPromotion.setType(promotionDTO.getType());
        existingPromotion.setApplicableFor(promotionDTO.getApplicableFor());
        existingPromotion.setDiscountType(promotionDTO.getDiscountType());
        existingPromotion.setDiscountValue(promotionDTO.getDiscountValue());
        existingPromotion.setStartDate(promotionDTO.getStartDate());
        existingPromotion.setEndDate(promotionDTO.getEndDate());
        existingPromotion.setMinOrderValue(promotionDTO.getMinOrderValue());
        existingPromotion.setMaxDiscountValue(promotionDTO.getMaxDiscountValue());
        existingPromotion.setUsageLimit(promotionDTO.getUsageLimit());
        existingPromotion.setCategoryId(promotionDTO.getCategoryId());

        promotionRepository.save(existingPromotion);
    }

    @Override
    public void updateStorePromotion(String promotionId, PromotionDTO promotionDTO, String ownerId) throws Exception {
        Promotion existingPromotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Check if promotion is PLATFORM - only admin can update
        if ("PLATFORM".equals(existingPromotion.getIssuer())) {
            throw new Exception("Không có quyền cập nhật khuyến mãi của platform. Chỉ admin mới có quyền này.");
        }

        // Check if user is the owner of the store
        if (existingPromotion.getStore() == null || 
            existingPromotion.getStore().getOwner() == null ||
            !existingPromotion.getStore().getOwner().getId().equals(ownerId)) {
            throw new Exception("Bạn không có quyền cập nhật khuyến mãi của cửa hàng này.");
        }

        // Validate maxDiscountValue based on type
        if ("FIXED_AMOUNT".equals(promotionDTO.getType()) && 
            promotionDTO.getMaxDiscountValue() != null && 
            promotionDTO.getMaxDiscountValue() > 0) {
            throw new Exception("Khuyến mãi FIXED_AMOUNT không được có maxDiscountValue");
        }

        // Update fields
        existingPromotion.setTitle(promotionDTO.getTitle());
        existingPromotion.setCode(promotionDTO.getCode());
        existingPromotion.setType(promotionDTO.getType());
        existingPromotion.setApplicableFor(promotionDTO.getApplicableFor());
        existingPromotion.setDiscountType(promotionDTO.getDiscountType());
        existingPromotion.setDiscountValue(promotionDTO.getDiscountValue());
        existingPromotion.setStartDate(promotionDTO.getStartDate());
        existingPromotion.setEndDate(promotionDTO.getEndDate());
        existingPromotion.setMinOrderValue(promotionDTO.getMinOrderValue());
        existingPromotion.setMaxDiscountValue(promotionDTO.getMaxDiscountValue());
        existingPromotion.setUsageLimit(promotionDTO.getUsageLimit());
        existingPromotion.setCategoryId(promotionDTO.getCategoryId());

        promotionRepository.save(existingPromotion);
    }

    @Override
    public Promotion getPromotionById(String promotionId) throws Exception {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
    }

    @Override
    public Page<Promotion> getAllPromotions(Pageable pageable) {
        return promotionRepository.findAll(pageable);
    }

    @Override
    public Page<PromotionResponse> getPromotionsByStore(String storeId, Pageable pageable) {
        return promotionRepository.findByStoreId(storeId, pageable)
                .map(PromotionResponse::fromPromotion);
    }


    @Override
    public void deletePromotion(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
        
        // Soft delete - set status to DELETED
        promotion.setStatus(Promotion.PromotionStatus.DELETED.name());
        promotionRepository.save(promotion);
    }

    @Override
    public void deleteStorePromotion(String promotionId, String ownerId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Check if promotion is PLATFORM - only admin can delete
        if ("PLATFORM".equals(promotion.getIssuer())) {
            throw new Exception("Không có quyền xóa khuyến mãi của platform. Chỉ admin mới có quyền này.");
        }

        // Check if user is the owner of the store
        if (promotion.getStore() == null || 
            promotion.getStore().getOwner() == null ||
            !promotion.getStore().getOwner().getId().equals(ownerId)) {
            throw new Exception("Bạn không có quyền xóa khuyến mãi của cửa hàng này.");
        }

        // Soft delete - set status to DELETED
        promotion.setStatus(Promotion.PromotionStatus.DELETED.name());
        promotionRepository.save(promotion);
    }

    @Override
    public void activatePromotion(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
        
        promotion.setStatus("ACTIVE");
        promotionRepository.save(promotion);
    }

    @Override
    public void deactivatePromotion(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
        
        promotion.setStatus("INACTIVE");
        promotionRepository.save(promotion);
    }

    @Override
    public void activateStorePromotion(String promotionId, String ownerId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Check if promotion is PLATFORM - only admin can activate
        if ("PLATFORM".equals(promotion.getIssuer())) {
            throw new Exception("Không có quyền kích hoạt khuyến mãi của platform. Chỉ admin mới có quyền này.");
        }

        // Check if user is the owner of the store
        if (promotion.getStore() == null || 
            promotion.getStore().getOwner() == null ||
            !promotion.getStore().getOwner().getId().equals(ownerId)) {
            throw new Exception("Bạn không có quyền kích hoạt khuyến mãi của cửa hàng này.");
        }
        
        promotion.setStatus("ACTIVE");
        promotionRepository.save(promotion);
    }

    @Override
    public void deactivateStorePromotion(String promotionId, String ownerId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Check if promotion is PLATFORM - only admin can deactivate
        if ("PLATFORM".equals(promotion.getIssuer())) {
            throw new Exception("Không có quyền vô hiệu hóa khuyến mãi của platform. Chỉ admin mới có quyền này.");
        }

        // Check if user is the owner of the store
        if (promotion.getStore() == null || 
            promotion.getStore().getOwner() == null ||
            !promotion.getStore().getOwner().getId().equals(ownerId)) {
            throw new Exception("Bạn không có quyền vô hiệu hóa khuyến mãi của cửa hàng này.");
        }
        
        promotion.setStatus("INACTIVE");
        promotionRepository.save(promotion);
    }

    @Override
    public Page<PromotionResponse> getActivePromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findActivePromotions(LocalDateTime.now(), pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getActivePromotionsByStore(String storeId, Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findActivePromotionsByStore(storeId, LocalDateTime.now(), pageable);
        return promotions.map(PromotionResponse::fromPromotion);
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
    public Page<PromotionResponse> getExpiredPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findExpiredPromotions(LocalDateTime.now(), pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getExpiredPlatformPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findExpiredPlatformPromotions(LocalDateTime.now(), pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getPromotionsByType(String type, Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findByType(type, pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getDeletedPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findDeletedPromotions(pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getDeletedPlatformPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findDeletedPlatformPromotions(pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<Promotion> getPlatformPromotions(Pageable pageable) {
        return promotionRepository.findByIssuer("PLATFORM", pageable);
    }

    @Override
    public Page<PromotionResponse> getInactivePlatformPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findInactivePlatformPromotions(pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getActivePlatformPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findActivePlatformPromotions(pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }
}
