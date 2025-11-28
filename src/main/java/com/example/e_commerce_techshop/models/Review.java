package com.example.e_commerce_techshop.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review extends BaseEntity {
    @Id
    private String id;

    private Integer rating; // 1-5

    private String comment;

    private List<String> imageUrls;

    @DBRef
    private Order order;

    @DBRef
    private ProductVariant productVariant;

    @DBRef
    private User user;
}
