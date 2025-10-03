package com.example.e_commerce_techshop.dtos.b2c.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @NotNull(message = "ID người mua không được để trống")
    private String buyerId;

    @NotNull(message = "ID cửa hàng không được để trống")
    private String storeId;

    private Map<String, String> promotionIds;

    @NotNull(message = "Tổng giá không được để trống")
    @Positive(message = "Tổng giá phải lớn hơn 0")
    private Long totalPrice;

    @NotNull(message = "ID địa chỉ không được để trống")
    private String addressId;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    private String status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    
    // Additional fields for response
    private String id;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer itemCount;
    private List<OrderItem> orderItems;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem {
        private String id;
        private String productVariantId;
        private String productName;
        private String variantName;
        private String productImage;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
        
        // Additional product info
        private String productId;
        private String category;
        private String brand;
        private Integer stock;
    }
}



