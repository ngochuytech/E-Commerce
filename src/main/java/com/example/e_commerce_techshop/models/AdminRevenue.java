package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.math.BigDecimal;

@Document(collection = "admin_revenues")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminRevenue extends BaseEntity {
    @Id
    private String id;

    @DBRef
    private Order order;  // Liên kết đến order

    private BigDecimal amount;  // Số tiền

    private String revenueType;  // SERVICE_FEE, COMMISSION (để mở rộng sau)

    private String description;
}
