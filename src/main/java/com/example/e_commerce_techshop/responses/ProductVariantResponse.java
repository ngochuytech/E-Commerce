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

    private String productId;

    private Long price;

    private String description;

    private int stock;

    private String status;

    private Map<String, String> attributes;

    private List<ColorResponse> colors;

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

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ColorResponse {
        private String id;
        private String name;
        private Long price;
        private int stock;
        private String image;
    }

    public static ProductVariantResponse fromProductVariant(ProductVariant productVariant){
        // Use the attributes Map directly from MongoDB
        Map<String, String> attributes = productVariant.getAttributes() != null ? 
            new HashMap<>(productVariant.getAttributes()) : new HashMap<>();
        
        List<ColorResponse> colors = productVariant.getColors() != null ? 
            productVariant.getColors().stream().map(color -> 
                ColorResponse.builder()
                    .id(color.getId())
                    .name(color.getColorName())
                    .price(color.getPrice())
                    .stock(color.getStock())
                    .image(color.getImage())
                    .build()
            ).toList() : List.of();

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
                .productId(productVariant.getProduct().getId())
                .images(imageUrls)
                .primaryImage(primaryImageUrl)
                .price(productVariant.getPrice())
                .description(productVariant.getDescription())
                .stock(productVariant.getStock())
                .status(productVariant.getStatus())
                .attributes(attributes)
                .colors(colors)
                .store(storeResponse)
                .build();
    }
}