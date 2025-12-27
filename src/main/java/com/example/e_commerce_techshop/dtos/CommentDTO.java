package com.example.e_commerce_techshop.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotBlank(message = "Product Variant ID is required")
    private String productVariantId;
    
    // Optional: for reply functionality
    private String parentCommentId;
}
