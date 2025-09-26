package com.example.e_commerce_techshop.dtos.buyer.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderResponseDTO {
    
    private String id;
    private String buyerId;
    private String storeId;
    private String promotionId;
    private BigDecimal totalPrice;
    private String addressId;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<OrderItemDTO> orderItems;
}
