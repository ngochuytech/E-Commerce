package com.example.e_commerce_techshop.responses.admin;

import com.example.e_commerce_techshop.models.Dispute;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response cho tranh chấp
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisputeResponse {

    private String id;
    private String returnRequestId;
    private String orderId;
    private String buyerId;
    private String buyerName;
    private String storeId;
    private String storeName;
    private String status;
    private List<DisputeMessageResponse> messages;
    private String adminHandlerId;
    private String adminHandlerName;
    private String finalDecision;
    private String decisionReason;
    private String winner;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DisputeMessageResponse {
        private String senderId;
        private String senderType;
        private String senderName;
        private String content;
        private List<String> attachments;
        private LocalDateTime sentAt;

        public static DisputeMessageResponse fromDisputeMessage(Dispute.DisputeMessage message) {
            return DisputeMessageResponse.builder()
                    .senderId(message.getSenderId())
                    .senderType(message.getSenderType())
                    .senderName(message.getSenderName())
                    .content(message.getContent())
                    .attachments(message.getAttachments())
                    .sentAt(message.getSentAt())
                    .build();
        }
    }

    public static DisputeResponse fromDispute(Dispute dispute) {
        return DisputeResponse.builder()
                .id(dispute.getId())
                .returnRequestId(dispute.getReturnRequest() != null ? dispute.getReturnRequest().getId() : null)
                .orderId(dispute.getOrder() != null ? dispute.getOrder().getId() : null)
                .buyerId(dispute.getBuyer() != null ? dispute.getBuyer().getId() : null)
                .buyerName(dispute.getBuyer() != null ? dispute.getBuyer().getFullName() : null)
                .storeId(dispute.getStore() != null ? dispute.getStore().getId() : null)
                .storeName(dispute.getStore() != null ? dispute.getStore().getName() : null)
                .status(dispute.getStatus())
                .messages(dispute.getMessages() != null ? 
                        dispute.getMessages().stream()
                                .map(DisputeMessageResponse::fromDisputeMessage)
                                .collect(Collectors.toList()) : null)
                .adminHandlerId(dispute.getAdminHandler() != null ? dispute.getAdminHandler().getId() : null)
                .adminHandlerName(dispute.getAdminHandler() != null ? dispute.getAdminHandler().getFullName() : null)
                .finalDecision(dispute.getFinalDecision())
                .decisionReason(dispute.getDecisionReason())
                .winner(dispute.getWinner())
                .resolvedAt(dispute.getResolvedAt())
                .createdAt(dispute.getCreatedAt())
                .updatedAt(dispute.getUpdatedAt())
                .build();
    }
}
