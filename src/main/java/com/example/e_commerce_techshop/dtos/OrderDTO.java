package com.example.e_commerce_techshop.dtos;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    @JsonProperty("payment_method")
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // COD, VNPAY, MOMO, etc.
    
    @JsonProperty("promotion_code")
    private String promotionCode;
    
    private String note;

    private AddressDTO address;

    @Data
    @Builder
    public static class AddressDTO {
        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        private String province;

        @NotBlank(message = "Quận/Huyện không được để trống")
        private String district;

        @NotBlank(message = "Phường/Xã không được để trống")
        private String ward;

        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        private String homeAddress;

        private String suggestedName;
    }
}
