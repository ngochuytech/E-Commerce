package com.example.e_commerce_techshop.services.promotion;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.exceptions.InvalidPromotionException;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.PromotionUsage;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.PromotionRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.repositories.PromotionUsageRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService implements IPromotionService {

    private final PromotionRepository promotionRepository;
    private final StoreRepository storeRepository;
    private final PromotionUsageRepository promotionUsageRepository;
    private final OrderRepository orderRepository;

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

        if (Promotion.PromotionType.FIXED_AMOUNT.name().equals(createPromotionDTO.getType()) &&
                createPromotionDTO.getMaxDiscountValue() != null &&
                createPromotionDTO.getMaxDiscountValue() > 0) {
            throw new Exception("Khuyến mãi FIXED_AMOUNT không được có maxDiscountValue");
        }

        Promotion promotion = Promotion.builder()
                .title(createPromotionDTO.getTitle())
                .code(createPromotionDTO.getCode())
                .description(createPromotionDTO.getDescription())
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
                .usageLimitPerUser(createPromotionDTO.getUsageLimitPerUser())
                .isNewUserOnly(
                        createPromotionDTO.getIsNewUserOnly() != null ? createPromotionDTO.getIsNewUserOnly() : false)
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
                .description(createPromotionDTO.getDescription())
                .store(null)
                .issuer(Promotion.Issuer.PLATFORM.name())
                .applicableFor(createPromotionDTO.getApplicableFor())
                .type(createPromotionDTO.getType())
                .discountType(createPromotionDTO.getDiscountType())
                .discountValue(createPromotionDTO.getDiscountValue())
                .startDate(createPromotionDTO.getStartDate())
                .endDate(createPromotionDTO.getEndDate())
                .maxDiscountValue(createPromotionDTO.getMaxDiscountValue())
                .usageLimit(createPromotionDTO.getUsageLimit())
                .usageLimitPerUser(createPromotionDTO.getUsageLimitPerUser())
                .isNewUserOnly(
                        createPromotionDTO.getIsNewUserOnly() != null ? createPromotionDTO.getIsNewUserOnly() : false)
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

        if ("FIXED_AMOUNT".equals(promotionDTO.getType()) &&
                promotionDTO.getMaxDiscountValue() != null &&
                promotionDTO.getMaxDiscountValue() > 0) {
            throw new Exception("Khuyến mãi FIXED_AMOUNT không được có maxDiscountValue");
        }

        // Update fields
        existingPromotion.setTitle(promotionDTO.getTitle());
        existingPromotion.setCode(promotionDTO.getCode());
        existingPromotion.setDescription(promotionDTO.getDescription());
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
        existingPromotion.setDescription(promotionDTO.getDescription());
        existingPromotion.setType(promotionDTO.getType());
        existingPromotion.setApplicableFor(promotionDTO.getApplicableFor());
        existingPromotion.setDiscountType(promotionDTO.getDiscountType());
        existingPromotion.setDiscountValue(promotionDTO.getDiscountValue());
        existingPromotion.setStartDate(promotionDTO.getStartDate());
        existingPromotion.setEndDate(promotionDTO.getEndDate());
        existingPromotion.setMinOrderValue(promotionDTO.getMinOrderValue());
        existingPromotion.setMaxDiscountValue(promotionDTO.getMaxDiscountValue());
        existingPromotion.setUsageLimit(promotionDTO.getUsageLimit());
        existingPromotion.setUsageLimitPerUser(promotionDTO.getUsageLimitPerUser());
        existingPromotion
                .setIsNewUserOnly(promotionDTO.getIsNewUserOnly() != null ? promotionDTO.getIsNewUserOnly() : false);
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

        if (promotion.getIssuer().equals(Promotion.Issuer.STORE.name())) {
            throw new Exception("Không có quyền xóa khuyến mãi của cửa hàng. Chỉ có chủ cửa hàng mới có quyền này.");
        }

        promotion.setStatus(Promotion.PromotionStatus.DELETED.name());
        promotionRepository.save(promotion);
    }

    @Override
    public void deleteStorePromotion(String promotionId, String ownerId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Check if promotion is PLATFORM - only admin can delete
        if (Promotion.Issuer.PLATFORM.name().equals(promotion.getIssuer())) {
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

        promotion.setStatus(Promotion.PromotionStatus.ACTIVE.name());
        promotionRepository.save(promotion);
    }

    @Override
    public void deactivatePromotion(String promotionId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        promotion.setStatus(Promotion.PromotionStatus.INACTIVE.name());
        promotionRepository.save(promotion);
    }

    @Override
    public void activateStorePromotion(String promotionId, String ownerId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        if (Promotion.Issuer.PLATFORM.name().equals(promotion.getIssuer())) {
            throw new Exception("Không có quyền kích hoạt khuyến mãi của platform. Chỉ admin mới có quyền này.");
        }

        if (promotion.getStore() == null ||
                promotion.getStore().getOwner() == null ||
                !promotion.getStore().getOwner().getId().equals(ownerId)) {
            throw new Exception("Bạn không có quyền kích hoạt khuyến mãi của cửa hàng này.");
        }

        promotion.setStatus(Promotion.PromotionStatus.ACTIVE.name());
        promotionRepository.save(promotion);
    }

    @Override
    public void deactivateStorePromotion(String promotionId, String ownerId) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        if (Promotion.Issuer.PLATFORM.name().equals(promotion.getIssuer())) {
            throw new Exception("Không có quyền vô hiệu hóa khuyến mãi của platform. Chỉ admin mới có quyền này.");
        }

        if (promotion.getStore() == null ||
                promotion.getStore().getOwner() == null ||
                !promotion.getStore().getOwner().getId().equals(ownerId)) {
            throw new Exception("Bạn không có quyền vô hiệu hóa khuyến mãi của cửa hàng này.");
        }

        promotion.setStatus(Promotion.PromotionStatus.INACTIVE.name());
        promotionRepository.save(promotion);
    }

    @Override
    public boolean validatePromotion(String promotionId, Long orderValue) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        LocalDateTime now = LocalDateTime.now();

        // Check if promotion is active and not expired
        if (!Promotion.PromotionStatus.ACTIVE.name().equals(promotion.getStatus()) ||
                promotion.getStartDate().isAfter(now) ||
                promotion.getEndDate().isBefore(now)) {
            throw new Exception("Khuyến mãi không còn hiệu lực");
        }

        // Check minimum order value
        if (orderValue < promotion.getMinOrderValue()) {
            throw new Exception("Đơn hàng không đạt giá trị tối thiểu cho khuyến mãi");
        }

        // Check total usage limit
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new Exception("Mã khuyến mãi đã hết lượt sử dụng");
        }

        return true;
    }

    @Override
    public boolean validatePromotionForUser(String promotionId, Long orderValue, User user) throws Exception {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));

        // Validate basic promotion rules
        validatePromotion(promotionId, orderValue);

        // Kiểm tra mã chỉ dành cho người dùng mới
        if (promotion.getIsNewUserOnly() != null && promotion.getIsNewUserOnly()) {
            long orderCount = orderRepository.countByBuyerId(user.getId());
            if (orderCount > 0) {
                throw new Exception("Mã khuyến mãi này chỉ dành cho người dùng mới");
            }
        }

        // Kiểm tra giới hạn sử dụng mỗi người
        if (promotion.getUsageLimitPerUser() != null) {
            int userUsageCount = promotionUsageRepository.countByPromotionAndUser(promotion, user);
            if (userUsageCount >= promotion.getUsageLimitPerUser()) {
                throw new Exception("Bạn đã sử dụng hết số lần cho phép với mã khuyến mãi này");
            }
        }

        return true;
    }

    @Override
    public void recordPromotionUsage(Promotion promotion, User user) throws Exception {
        // Update promotion used count
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);

        // Record usage in history
        PromotionUsage usage = PromotionUsage.builder()
                .promotion(promotion)
                .user(user)
                .usedAt(LocalDateTime.now())
                .usageCount(promotionUsageRepository.countByPromotionAndUser(promotion, user) + 1)
                .build();

        promotionUsageRepository.save(usage);
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
    public Page<PromotionResponse> getDeletedPlatformPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findDeletedPlatformPromotions(pageable);
        return promotions.map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getPlatformPromotionsForCustomer(Long orderValue, User user, Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findByIssuerForCustomer(Promotion.Issuer.PLATFORM.name(),
                LocalDateTime.now(),
                pageable);
        List<PromotionResponse> applicablePromotions = promotions.getContent().stream()
                .filter(promotion -> {
                    try {
                        // Kiểm tra điều kiện áp dụng
                        if (orderValue < promotion.getMinOrderValue()) {
                            return false; // Không đủ giá trị đơn hàng tối thiểu
                        }

                        // Kiểm tra usage limit
                        if (promotion.getUsageLimit() != null &&
                                promotion.getUsedCount() >= promotion.getUsageLimit()) {
                            return false; // Đã hết số lần sử dụng
                        }

                        // Kiểm tra new user only
                        if (user != null && promotion.getIsNewUserOnly() != null && promotion.getIsNewUserOnly()) {
                            long orderCount = orderRepository.countByBuyerId(user.getId());
                            if (orderCount > 0) {
                                return false; // Không phải user mới
                            }
                        }

                        // Kiểm tra usage limit per user
                        if (user != null && promotion.getUsageLimitPerUser() != null) {
                            int userUsageCount = promotionUsageRepository.countByPromotionAndUser(promotion, user);
                            if (userUsageCount >= promotion.getUsageLimitPerUser()) {
                                return false; // Đã vượt quá giới hạn sử dụng của user
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(PromotionResponse::fromPromotion)
                .collect(Collectors.toList());

        return new PageImpl<>(applicablePromotions, pageable, applicablePromotions.size());
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

    @Override
    public Page<Promotion> getAllPromotionForCustomer(Pageable pageable) {
        return promotionRepository.findPromotionForCustomer(LocalDateTime.now(), pageable);
    }

    @Override
    public Page<PromotionResponse> getStorePromotionsForCustomer(String storeId, Long orderValue, User user,
            Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findByStoreIdForCustomer(storeId, LocalDateTime.now(),
                pageable);
        // Filter các mã có thể áp dụng cho đơn hàng và convert sang PromotionResponse
        List<PromotionResponse> applicablePromotions = promotions.getContent().stream()
                .filter(promotion -> {
                    try {
                        // Kiểm tra điều kiện áp dụng
                        if (orderValue < promotion.getMinOrderValue()) {
                            return false; // Không đủ giá trị đơn hàng tối thiểu
                        }

                        // Kiểm tra issuer (nếu có storeId)
                        if (storeId != null && !storeId.isEmpty()) {
                            if (Promotion.Issuer.STORE.name().equals(promotion.getIssuer())) {
                                if (promotion.getStore() == null ||
                                        !promotion.getStore().getId().equals(storeId)) {
                                    return false; // Mã của store khác
                                }
                            }
                        }

                        // Kiểm tra usage limit
                        if (promotion.getUsageLimit() != null &&
                                promotion.getUsedCount() >= promotion.getUsageLimit()) {
                            return false; // Đã hết số lần sử dụng
                        }

                        // Kiểm tra new user only
                        if (user != null && promotion.getIsNewUserOnly() != null && promotion.getIsNewUserOnly()) {
                            long orderCount = orderRepository.countByBuyerId(user.getId());
                            if (orderCount > 0) {
                                return false; // Không phải user mới
                            }
                        }

                        // Kiểm tra usage limit per user
                        if (user != null && promotion.getUsageLimitPerUser() != null) {
                            int userUsageCount = promotionUsageRepository.countByPromotionAndUser(promotion, user);
                            if (userUsageCount >= promotion.getUsageLimitPerUser()) {
                                return false; // Đã vượt quá giới hạn sử dụng của user
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(PromotionResponse::fromPromotion)
                .collect(Collectors.toList());

        return new PageImpl<>(applicablePromotions, pageable, applicablePromotions.size());
    }

    @Override
    public Promotion getPromotionByCode(String promotionCode) throws Exception {
        return promotionRepository.findByCode(promotionCode)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
    }

    @Override
    public Page<PromotionResponse> getPromotionsByStoreAndStatus(String storeId, String status, Pageable pageable) {
        return promotionRepository.findByStoreIdAndStatus(storeId, status, pageable)
                .map(PromotionResponse::fromPromotion);
    }

    @Override
    public Page<PromotionResponse> getExpiredPromotionsByStore(String storeId, Pageable pageable) {
        return promotionRepository.findExpiredPromotionsByStoreId(storeId, LocalDateTime.now(), pageable)
                .map(PromotionResponse::fromPromotion);
    }

    @Override
    public void validatePromotionForUser(Promotion promotion, Long orderValue, User user)
            throws InvalidPromotionException {
        // Check if promotion is active and not expired
        LocalDateTime now = LocalDateTime.now();

        if (!Promotion.PromotionStatus.ACTIVE.name().equals(promotion.getStatus()) ||
                promotion.getStartDate().isAfter(now) ||
                promotion.getEndDate().isBefore(now)) {
            throw new InvalidPromotionException("Khuyến mãi không còn hiệu lực");
        }

        // Check minimum order value
        if (orderValue < promotion.getMinOrderValue()) {
            throw new InvalidPromotionException("Giá trị đơn hàng không đủ để áp dụng khuyến mãi");
        }

        // Check total usage limit
        if (promotion.getUsageLimit() != null && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            throw new InvalidPromotionException("Khuyến mãi đã đạt giới hạn sử dụng");
        }
        // Kiểm tra mã chỉ dành cho người dùng mới
        if (promotion.getIsNewUserOnly() != null && promotion.getIsNewUserOnly()) {
            long orderCount = orderRepository.countByBuyerId(user.getId());
            if (orderCount > 0) {
                throw new InvalidPromotionException("Khuyến mãi chỉ áp dụng cho người dùng mới");
            }
        }

        // Kiểm tra giới hạn sử dụng mỗi người
        if (promotion.getUsageLimitPerUser() != null) {
            int userUsageCount = promotionUsageRepository.countByPromotionAndUser(promotion, user);
            if (userUsageCount >= promotion.getUsageLimitPerUser()) {
                throw new InvalidPromotionException("Khuyến mãi đã đạt giới hạn sử dụng cho người dùng");
            }
        }
    }
}
