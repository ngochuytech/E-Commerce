package com.example.e_commerce_techshop.dtos.b2c.store;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("banner_url")
    private String bannerUrl;

    // Optional on create; default will be PENDING
    private String status;

    @JsonProperty("owner_id")
    private String ownerId;

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