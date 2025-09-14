package com.example.e_commerce_techshop.dtos.b2c.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "The maximum length of the name is 255 characters")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;

    @JsonProperty("price")
    private Long price;

    @JsonProperty("product_condition")
    @NotNull(message = "Product condition is required")
    private String productCondition;

    @JsonProperty("status")
    @NotNull(message = "Status is required")
    private String status;

    @JsonProperty("brand_id")
    @NotNull(message = "Brand id is required")
    private String brandId;

    @JsonProperty("store_id")
    @NotNull(message = "Store id is required")
    private String storeId;
}



