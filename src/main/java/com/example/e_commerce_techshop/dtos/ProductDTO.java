package com.example.e_commerce_techshop.dtos;

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

    private Long price;

    @NotNull(message = "Brand is required")
    private String brand;

    @NotNull(message = "Store id is required")
    private String storeId;
}
