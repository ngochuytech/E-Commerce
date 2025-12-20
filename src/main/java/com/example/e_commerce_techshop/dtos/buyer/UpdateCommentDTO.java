package com.example.e_commerce_techshop.dtos.buyer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDTO {
    
    @NotBlank(message = "Content is required")
    private String content;
}
