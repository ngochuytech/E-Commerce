package com.example.e_commerce_techshop.dtos.b2c.store;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreDTO {
    @NotBlank(message = "Tên cửa hàng không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Địa chỉ không được để trống")
    @Valid
    private AddressDTO address;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressDTO {
        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        private String province;

        @NotBlank(message = "Phường/Xã không được để trống")
        private String ward;

        @NotBlank(message = "Địa chỉ nhà không được để trống")
        private String homeAddress;

        private String suggestedName;
    }
}