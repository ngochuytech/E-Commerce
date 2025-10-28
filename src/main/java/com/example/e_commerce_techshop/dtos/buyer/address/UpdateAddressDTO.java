package com.example.e_commerce_techshop.dtos.buyer.address;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAddressDTO {
    
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 255, message = "Tỉnh/Thành phố không được vượt quá 255 ký tự")
    private String province;

    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 255, message = "Phường/Xã không được vượt quá 255 ký tự")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
    private String homeAddress;

    @Size(max = 255, message = "Tên gợi ý không được vượt quá 255 ký tự")
    private String suggestedName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 10, message = "Số điện thoại phải đúng 10 ký tự")
    private String phone;
    
    private boolean isDefault;
}
