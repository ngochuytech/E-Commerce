package com.example.e_commerce_techshop.dtos.b2c.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusDTO {
    
    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    @Pattern(regexp = "^(PENDING|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED)$", 
             message = "Trạng thái không hợp lệ")
    private String status;
    
    private String notes;
    private String trackingNumber;
}
