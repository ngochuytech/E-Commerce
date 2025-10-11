package com.example.e_commerce_techshop.models;

import lombok.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "promotions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Promotion extends BaseEntity {
    @Id
    private String id;

    private String title;

    @DBRef
    private Store store;

    private String type; // PERCENTAGE, FIXED_AMOUNT

    private String discountType; // PRODUCT, ORDER, CATEGORY

    private Long discountValue;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long minOrderValue;

    private Long maxDiscountValue;

    private String status;

    @Indexed(unique = true)
    private String code; // Mã giảm giá để người dùng nhập
}



