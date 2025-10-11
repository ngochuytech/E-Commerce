package com.example.e_commerce_techshop.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BrandDTO {
    private String id;
    
    @NotBlank(message = "Brand name is required")
    @Size(min = 1, max = 100, message = "Brand name must be between 1 and 100 characters")
    private String name;
}