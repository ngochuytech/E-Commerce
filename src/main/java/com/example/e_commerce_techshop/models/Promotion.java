package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Promotion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "type", nullable = false)
    private String type; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_type", nullable = false)
    private String discountType; // PRODUCT, ORDER, CATEGORY

    @Column(name = "discount_value")
    private Long discountValue;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "min_order_value")
    private Long minOrderValue;

    @Column(name = "max_discount_value")
    private Long maxDiscountValue;

    @Column(name = "status")
    private String status; // ACTIVE, INACTIVE, EXPIRED
    
    @Column(name = "code", unique = true)
    private String code; // Mã giảm giá để người dùng nhập
}



