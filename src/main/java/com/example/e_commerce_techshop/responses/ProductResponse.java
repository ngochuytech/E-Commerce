package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Product;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private String id;

    private String name;

    private String category;

    private String description;

    private Long price;

    private String condition;

    private String status;

    private String brand;

    private String store;

    public static ProductResponse fromProduct(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .price(product.getPrice())
                .condition(product.getProductCondition())
                .status(product.getStatus())
                .store(product.getStore().getName())
                .build();
    }
}
