package com.example.e_commerce_techshop.dtos.b2c.store;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("logo_url")
    private String logoUrl;

    @JsonProperty("banner_url")
    private String bannerUrl;

    // Optional on create; default will be PENDING
    private String status;

    @JsonProperty("owner_id")
    private String ownerId;

    @JsonProperty("address_id")
    private String addressId;
}