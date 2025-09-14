package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Promotion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PromotionResponse {
    private String id;
    private String title;
    private String storeId;
    private String storeName;
    private String type;
    private String discountType;
    private Long discountValue;
    private String startDate;
    private String endDate;
    private Long minOrderValue;
    private Long maxDiscountValue;
    private String status;
    private String createdAt;
    private String updatedAt;

    public static PromotionResponse fromPromotion(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .title(promotion.getTitle())
                .storeId(promotion.getStore() != null ? promotion.getStore().getId() : null)
                .storeName(promotion.getStore() != null ? promotion.getStore().getName() : null)
                .type(promotion.getType())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .startDate(promotion.getStartDate() != null ? promotion.getStartDate().toString() : null)
                .endDate(promotion.getEndDate() != null ? promotion.getEndDate().toString() : null)
                .minOrderValue(promotion.getMinOrderValue())
                .maxDiscountValue(promotion.getMaxDiscountValue())
                .status(promotion.getStatus())
                .createdAt(promotion.getCreatedAt() != null ? promotion.getCreatedAt().toString() : null)
                .updatedAt(promotion.getUpdatedAt() != null ? promotion.getUpdatedAt().toString() : null)
                .build();
    }
}



