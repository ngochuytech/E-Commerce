package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ProductVariantAttribute;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariantResponse {
    private String name;

    private String imageUrl;

    private Long price;

    private String description;

    private int stock;

    private Map<String, String> attributes;

    public static ProductVariantResponse fromProductVariant(ProductVariant productVariant){
        Map<String, String> attributes = new HashMap<>();
        for (ProductVariantAttribute variantAttribute : productVariant.getAttributes()) {
            attributes.put(variantAttribute.getAttribute().getName(), variantAttribute.getValue());
        }

        return ProductVariantResponse.builder()
                .name(productVariant.getName())
                .imageUrl(productVariant.getImageUrl())
                .price(productVariant.getPrice())
                .description(productVariant.getDescription())
                .stock(productVariant.getStock())
                .attributes(attributes)
                .build();
    }


}