package com.example.e_commerce_techshop.dtos.buyer.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CheckoutDTO {
    
    @NotBlank(message = "ID địa chỉ không được để trống")
    private String addressId;
    
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Size(max = 50, message = "Phương thức thanh toán không được quá 50 ký tự")
    private String paymentMethod;

    // Optional: Mã khuyến mãi cấp độ đơn (thuộc cùng store của đơn)
    private String promotionId;
}
