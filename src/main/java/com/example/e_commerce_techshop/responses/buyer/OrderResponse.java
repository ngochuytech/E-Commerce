package com.example.e_commerce_techshop.responses.buyer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.e_commerce_techshop.models.Order;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String id;

    private BigDecimal totalPrice; // Giá cuối cùng khách phải thanh toán

    private BigDecimal productPrice; // Giá sản phẩm (tổng)

    private BigDecimal shippingFee; // Phí ship

    private BigDecimal storeDiscountAmount; // Tiền giảm từ shop (shop chịu)

    private BigDecimal platformDiscountAmount; // Tiền giảm từ sàn (sàn chịu)

    private BigDecimal totalDiscountAmount; // Tổng tiền giảm giá

    private String paymentMethod;

    private String momoTransId; // Mã giao dịch MoMo (transId khi thanh toán thành công)

    private String paymentStatus; // Trạng thái thanh toán: UNPAID, PAID, FAILED
    
    // Thông tin hoàn tiền thủ công (COD)
    private String manualRefundTransactionRef; // Mã giao dịch chuyển khoản thủ công
    private LocalDateTime manualRefundTransferredAt; // Thời điểm admin chuyển tiền
    private String manualRefundNote; // Ghi chú hoàn tiền thủ công

    private String status; // PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED

    private boolean isRated;
    
    private String returnRequestId; // Đơn hàng đã có yêu cầu trả hàng chưa

    private RefundInfo refundInfo; // Thông tin hoàn tiền (nếu có)

    private String shipmentId; // ID của shipment giao hàng

    private String returnShipmentId; // ID của shipment trả hàng

    private List<PromotionResponse> promotions; // Danh sách mã khuyến mãi được sử dụng

    private List<OrderItemResponse> orderItems;

    private UserResponse buyer;

    private StoreResponse store;

    private AddressResponse address;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class UserResponse {
        private String id;
        private String username;
        private String fullName;
        private String email;
        private String phone;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class StoreResponse {
        private String id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RefundInfo {
        private String refundRequestId;        // ID của RefundRequest
        private BigDecimal refundAmount;       // Số tiền hoàn
        private String refundMethod;           // BANK_TRANSFER, E_WALLET (MoMo), VNPAY
        private String status;                 // PENDING, COMPLETED, REJECTED
        private String refundTransactionId;    // Mã giao dịch hoàn tiền
        private LocalDateTime refundCompletedAt; // Thời điểm hoàn tiền thành công
        
        // Thông tin ngân hàng (nếu hoàn qua bank)
        private String bankName;
        private String bankAccountNumber;
        private String bankAccountName;
        
        // Thông tin hoàn tiền thủ công
        private String adminNote;
        private String rejectionReason;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PromotionResponse {
        private String id;
        private String code;                // Mã khuyến mãi
        private String title;               // Tiêu đề khuyến mãi
        private String description;         // Mô tả
        private String type;                // PERCENTAGE, FIXED_AMOUNT
        private String issuer;              // PLATFORM, STORE
        private String applicableFor;       // ORDER, SHIPPING
        private String discountType;        // ORDER, CATEGORY
        private Long discountValue;         // Giá trị giảm
        private Long minOrderValue;         // Giá trị đơn hàng tối thiểu
        private Long maxDiscountValue;      // Giảm giá tối đa
        private LocalDateTime startDate;    // Ngày bắt đầu
        private LocalDateTime endDate;      // Ngày kết thúc
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class AddressResponse {
        private String province;
        private String district;
        private String ward;
        private String homeAddress;
        private String suggestedName;
        private String phone;
    }

    public static OrderResponse fromOrder(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(orderItem -> {
                    String productImage = orderItem.getProductVariant().getPrimaryImageUrl();
                    
                    String productName = orderItem.getProductVariant().getProduct().getName();
                    String colorId = orderItem.getColorId();
                    
                    // Tìm tên màu và ảnh màu nếu có
                    String colorName = null;
                    if (colorId != null && orderItem.getProductVariant().getColors() != null) {
                        var colorOptional = orderItem.getProductVariant().getColors().stream()
                                .filter(color -> color.getId().equals(colorId))
                                .findFirst();
                        
                        if (colorOptional.isPresent()) {
                            var color = colorOptional.get();
                            colorName = color.getColorName();
                            if (color.getImage() != null && !color.getImage().isEmpty()) {
                                productImage = color.getImage();
                            }
                        }
                    }
                    
                    // Nối tên màu vào tên sản phẩm nếu có
                    if (colorName != null && !colorName.isEmpty()) {
                        productName = productName + " - " + colorName;
                    }

                    return OrderItemResponse.builder()
                            .id(orderItem.getId())
                            .productVariantId(orderItem.getProductVariant().getId())
                            .productName(productName)
                            .productImage(productImage)
                            .quantity(orderItem.getQuantity())
                            .price(BigDecimal.valueOf(orderItem.getPrice()))
                            .colorId(orderItem.getColorId())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        UserResponse buyer = UserResponse.builder()
                .id(order.getBuyer().getId())
                .username(order.getBuyer().getUsername())
                .email(order.getBuyer().getEmail())
                .phone(order.getBuyer().getPhone())
                .fullName(order.getBuyer().getFullName())
                .build();

        StoreResponse store = StoreResponse.builder()
                .id(order.getStore().getId())
                .name(order.getStore().getName())
                .logo(order.getStore().getLogoUrl())
                .build();

        AddressResponse address = AddressResponse.builder()
                .province(order.getAddress().getProvince())
                .ward(order.getAddress().getWard())
                .homeAddress(order.getAddress().getHomeAddress())
                .suggestedName(order.getAddress().getSuggestedName())
                .phone(order.getAddress().getPhone())
                .build();

        // Map promotions
        List<PromotionResponse> promotions = null;
        if (order.getPromotions() != null && !order.getPromotions().isEmpty()) {
            promotions = order.getPromotions().stream()
                    .map(promotion -> PromotionResponse.builder()
                            .id(promotion.getId())
                            .code(promotion.getCode())
                            .title(promotion.getTitle())
                            .description(promotion.getDescription())
                            .type(promotion.getType())
                            .issuer(promotion.getIssuer())
                            .applicableFor(promotion.getApplicableFor())
                            .discountType(promotion.getDiscountType())
                            .discountValue(promotion.getDiscountValue())
                            .minOrderValue(promotion.getMinOrderValue())
                            .maxDiscountValue(promotion.getMaxDiscountValue())
                            .startDate(promotion.getStartDate())
                            .endDate(promotion.getEndDate())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
        }

        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .productPrice(order.getProductPrice() != null ? order.getProductPrice() : BigDecimal.ZERO)
                .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO)
                .storeDiscountAmount(order.getStoreDiscountAmount() != null ? order.getStoreDiscountAmount() : BigDecimal.ZERO)
                .platformDiscountAmount(order.getPlatformDiscountAmount() != null ? order.getPlatformDiscountAmount() : BigDecimal.ZERO)
                .totalDiscountAmount(order.getTotalDiscountAmount() != null ? order.getTotalDiscountAmount() : BigDecimal.ZERO)
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .momoTransId(order.getMomoTransId())
                .manualRefundTransactionRef(order.getManualRefundTransactionRef())
                .manualRefundTransferredAt(order.getManualRefundTransferredAt())
                .manualRefundNote(order.getManualRefundNote())
                .status(order.getStatus())
                .isRated(order.isRated())
                .returnRequestId(order.getReturnRequestId())
                .shipmentId(order.getShipmentId())
                .returnShipmentId(order.getReturnShipmentId())
                .promotions(promotions)
                .orderItems(orderItems)
                .buyer(buyer)
                .store(store)
                .address(address)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private String id;

        private String productVariantId;

        private String productName;

        private String productImage;

        private Integer quantity;

        private BigDecimal price;

        private String colorId;
    }
}