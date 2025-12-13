package com.example.e_commerce_techshop.responses.buyer;

import com.example.e_commerce_techshop.models.ReturnRequest;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response cho yêu cầu trả hàng
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnRequestResponse {

    private String id;
    private String orderId;
    private String buyerId;
    private String buyerName;
    private String storeId;
    private String storeName;
    private String reason;
    private String description;
    private List<String> evidenceImages;
    private BigDecimal refundAmount;
    private String status;
    private String storeResponse;
    private String storeRejectReason;
    private List<String> storeEvidenceImages;
    private String adminDecision;
    private String adminDecisionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReturnRequestResponse fromReturnRequest(ReturnRequest returnRequest) {
        return ReturnRequestResponse.builder()
                .id(returnRequest.getId())
                .orderId(returnRequest.getOrder() != null ? returnRequest.getOrder().getId() : null)
                .buyerId(returnRequest.getBuyer() != null ? returnRequest.getBuyer().getId() : null)
                .buyerName(returnRequest.getBuyer() != null ? returnRequest.getBuyer().getFullName() : null)
                .storeId(returnRequest.getStore() != null ? returnRequest.getStore().getId() : null)
                .storeName(returnRequest.getStore() != null ? returnRequest.getStore().getName() : null)
                .reason(returnRequest.getReason())
                .description(returnRequest.getDescription())
                .evidenceImages(returnRequest.getEvidenceMedia())
                .refundAmount(returnRequest.getRefundAmount())
                .status(returnRequest.getStatus())
                .storeResponse(returnRequest.getStoreResponse())
                .storeRejectReason(returnRequest.getStoreRejectReason())
                .storeEvidenceImages(returnRequest.getStoreEvidenceMedia())
                .adminDecision(returnRequest.getAdminDecision())
                .adminDecisionReason(returnRequest.getAdminDecisionReason())
                .createdAt(returnRequest.getCreatedAt())
                .updatedAt(returnRequest.getUpdatedAt())
                .build();
    }
}
