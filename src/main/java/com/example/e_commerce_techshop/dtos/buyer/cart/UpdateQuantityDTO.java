package com.example.e_commerce_techshop.dtos.buyer.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQuantityDTO {
    @NotNull
    @Min(1)
    @Max(999)
    private Integer quantity;
}