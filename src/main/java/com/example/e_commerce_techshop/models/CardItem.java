package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "cart_id", nullable = false)
    private String cartId;
    
    @Column(name = "product_variant_id", nullable = false)
    private String productVariantId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // Relationship với Cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;
    
    // Relationship với ProductVariant (không cần @JoinColumn vì chỉ lưu productVariantId)
    // Có thể thêm @ManyToOne nếu cần lazy loading
}
