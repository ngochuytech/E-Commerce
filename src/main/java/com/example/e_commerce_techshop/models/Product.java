package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

enum ProductStatus {
    ACTIVE, HIDDEN, SOLD
}

@Document(collection = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseEntity{
    @Id
    private String id;

    private String name;

    @DBRef
    private Category category;

    private String description;

    private Long price;

    private String status; // PENDING, APPROVED, REJECTED

    private String rejectionReason;

    @DBRef
    private Brand brand;

    @DBRef
    private Store store;

    public enum ProductStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public static boolean isValidStatus(String status){
        try {
            ProductStatus.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
