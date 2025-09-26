package com.example.e_commerce_techshop.dtos.buyer.order;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderSummaryDTO {
    
    private String id;
    private BigDecimal totalPrice;
    private String status;
    private Integer itemCount;
    private LocalDateTime createdAt;
}
