package com.example.e_commerce_techshop.dtos.b2c.store;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreDTO {
    @NotBlank(message = "Tên cửa hàng không được để trống")
    private String name;

    private String description;

    private String logoUrl;

    private String bannerUrl;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    private String ownerId;

    private String addressId;
}