package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.ProductVariant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductVariantResponse {
    private String id;
    private String name;
    private String imageUrl;
    private Long price;
    private String description;
    private Integer stock;
    private String productId;
    private String productName;
    private String createdAt;
    private String updatedAt;

    public static ProductVariantResponse fromProductVariant(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .name(variant.getName())
                .imageUrl(variant.getImage_url())
                .price(variant.getPrice())
                .description(variant.getDescription())
                .stock(variant.getStock())
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .createdAt(variant.getId() != null ? "N/A" : "N/A") // ProductVariant không extend BaseEntity
                .updatedAt(variant.getId() != null ? "N/A" : "N/A")
                .build();
    }
}



