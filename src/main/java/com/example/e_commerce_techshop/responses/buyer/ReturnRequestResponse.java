package com.example.e_commerce_techshop.responses.buyer;

import com.example.e_commerce_techshop.models.Dispute;
import com.example.e_commerce_techshop.models.ReturnRequest;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    // Partial refund information
    private BigDecimal partialRefundToBuyer;
    private BigDecimal partialRefundToStore;
    
    // Related disputes
    private List<DisputeSummary> relatedDisputes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Thông tin tóm tắt về dispute liên quan
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DisputeSummary {
        private String disputeId;
        private String disputeType; // RETURN_REJECTION, RETURN_QUALITY
        private String status; // OPEN, IN_REVIEW, RESOLVED, CLOSED
        private String winner; // BUYER, STORE, null
        private String finalDecision;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;
        private int messageCount;
        
        public static DisputeSummary fromDispute(Dispute dispute) {
            return DisputeSummary.builder()
                    .disputeId(dispute.getId())
                    .disputeType(dispute.getDisputeType())
                    .status(dispute.getStatus())
                    .winner(dispute.getWinner())
                    .finalDecision(dispute.getFinalDecision())
                    .createdAt(dispute.getCreatedAt())
                    .resolvedAt(dispute.getResolvedAt())
                    .messageCount(dispute.getMessages() != null ? dispute.getMessages().size() : 0)
                    .build();
        }
    }

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
                .partialRefundToBuyer(returnRequest.getPartialRefundToBuyer())
                .partialRefundToStore(returnRequest.getPartialRefundToStore())
                .relatedDisputes(new ArrayList<>()) // Will be populated by service layer if needed
                .createdAt(returnRequest.getCreatedAt())
                .updatedAt(returnRequest.getUpdatedAt())
                .build();
    }
    
    /**
     * Helper method to add disputes to response
     */
    public static ReturnRequestResponse fromReturnRequestWithDisputes(ReturnRequest returnRequest, List<Dispute> disputes) {
        ReturnRequestResponse response = fromReturnRequest(returnRequest);
        if (disputes != null && !disputes.isEmpty()) {
            response.setRelatedDisputes(disputes.stream()
                    .map(DisputeSummary::fromDispute)
                    .toList());
        }
        return response;
    }
}
