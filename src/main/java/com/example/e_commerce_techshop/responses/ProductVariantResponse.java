package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.ProductVariant;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private StoreResponse store;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StoreResponse {
        private String id;
        private String name;
        private String logo;
    }

    public static ProductVariantResponse fromProductVariant(ProductVariant productVariant){
        // Use the attributes Map directly from MongoDB
        Map<String, String> attributes = productVariant.getAttributes() != null ? 
            new HashMap<>(productVariant.getAttributes()) : new HashMap<>();

        StoreResponse storeResponse = StoreResponse.builder()
                .id(productVariant.getProduct().getStore().getId())
                .name(productVariant.getProduct().getStore().getName())
                .logo(productVariant.getProduct().getStore().getLogoUrl())
                .build();

        // Use imageUrls List directly from MongoDB
        List<String> imageUrls = productVariant.getImageUrls() != null ? 
            productVariant.getImageUrls() : List.of();
        
        // Use primaryImageUrl directly from MongoDB, fallback to first image if not set
        String primaryImageUrl = productVariant.getPrimaryImageUrl();
        if (primaryImageUrl == null && !imageUrls.isEmpty()) {
            primaryImageUrl = imageUrls.get(0);
        }

        return ProductVariantResponse.builder()
                .id(productVariant.getId())
                .name(productVariant.getName())
                .images(imageUrls)
                .primaryImage(primaryImageUrl)
                .price(productVariant.getPrice())
                .description(productVariant.getDescription())
                .stock(productVariant.getStock())
                .attributes(attributes)
                .store(storeResponse)
                .build();
    }
}