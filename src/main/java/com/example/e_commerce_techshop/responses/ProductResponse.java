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

    private String status;

    private String brand;

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

    public static ProductResponse fromProduct(Product product){
        StoreResponse storeResponse = StoreResponse.builder()
                .id(product.getStore().getId())
                .name(product.getStore().getName())
                .logo(product.getStore().getLogoUrl())
                .build();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory().getDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus())
                .store(storeResponse)
                .build();
    }
}
