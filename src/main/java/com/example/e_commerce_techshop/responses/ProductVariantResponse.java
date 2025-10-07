package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.ProductImage;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ProductVariantAttribute;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantResponse {
    private String id;
    
    private String name;

    private List<String> images;
    
    @JsonProperty("primary_image")
    private String primaryImage;

    private Long price;

    private String description;

    private int stock;

    private Map<String, String> attributes;

    public static ProductVariantResponse fromProductVariant(ProductVariant productVariant){
        Map<String, String> attributes = new HashMap<>();
        for (ProductVariantAttribute variantAttribute : productVariant.getAttributes()) {
            attributes.put(variantAttribute.getAttribute().getName(), variantAttribute.getValue());
        }

        // Xử lý danh sách ảnh
        List<String> imageUrls = productVariant.getImages().stream()
                .map(ProductImage::getMediaPath)
                .collect(Collectors.toList());
        
        // Tìm ảnh chính
        String primaryImageUrl = productVariant.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImage::getMediaPath)
                .findFirst()
                .orElse(imageUrls.isEmpty() ? null : imageUrls.get(0));

        return ProductVariantResponse.builder()
                .id(productVariant.getId())
                .name(productVariant.getName())
                .images(imageUrls)
                .primaryImage(primaryImageUrl)
                .price(productVariant.getPrice())
                .description(productVariant.getDescription())
                .stock(productVariant.getStock())
                .attributes(attributes)
                .build();
    }
}