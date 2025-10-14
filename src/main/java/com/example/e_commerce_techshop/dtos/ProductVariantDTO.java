package com.example.e_commerce_techshop.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTO {
    @NotNull(message = "Name is required")
    private String name;
    
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be a positive number")
    private Long price;

    private String description;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be a positive number")
    private int stock;

    private Map<String, String> attributes;

    @NotNull(message = "ProductId is required")
    private String productId;

    private List<ColorOption> colors;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColorOption {
        @NotNull(message = "Color name is required")
        private String colorName;
        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be a positive number")
        private Long price;
        @NotNull(message = "Stock is required")
        @PositiveOrZero(message = "Stock must be a positive number")
        private int stock;
        @NotNull(message = "Image is required")
        private String image;
    }
}
