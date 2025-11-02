package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "promotion_usages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@CompoundIndex(name = "promotion_user_idx", def = "{'promotion': 1, 'user': 1}")
public class PromotionUsage extends BaseEntity {
    @Id
    private String id;

    @DBRef
    private Promotion promotion;

    @DBRef
    private User user;

    @DBRef
    private Order order;

    private LocalDateTime usedAt;

    private Integer usageCount;
}
