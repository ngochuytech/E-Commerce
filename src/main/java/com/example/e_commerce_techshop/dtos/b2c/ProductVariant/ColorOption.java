package com.example.e_commerce_techshop.dtos.b2c.ProductVariant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColorOption {
    @NotNull(message = "Color name is required")
    private String colorName;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be a positive number")
    private Long price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be a positive number")
    private int stock;
}
