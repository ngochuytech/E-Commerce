package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;
import java.util.Map;

@Document(collection = "product_variants")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {
    @Id
    private String id;

    private String name;

    private Long price;

    private String description;

    private int stock;

    @DBRef
    private Product product;

    // Store attributes as Map instead of separate entity
    private Map<String, String> attributes;

    // Store image URLs directly instead of separate entity
    private List<String> imageUrls;
    
    private String primaryImageUrl;
}
