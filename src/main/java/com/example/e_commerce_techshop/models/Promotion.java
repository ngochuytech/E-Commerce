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

    private String description;

    @DBRef
    private Store store;

    // LOẠI GIẢM GIÁ: Phần trăm hay số tiền cố định
    private String type;

    // NGƯỜI TẠO: PLATFORM (hệ thống) hay STORE (cửa hàng)
    private String issuer; 

    // ÁP DỤNG CHO: Đơn hàng hay vận chuyển
    private String applicableFor;

    private String discountType;

    private Long discountValue;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long minOrderValue;

    private Long maxDiscountValue;

    private Integer usageLimit; // Số lần sử dụng tối đa (NULL = không giới hạn)
    
    private Integer usedCount; // Số lần đã sử dụng

    private Integer usageLimitPerUser; // Số lần mỗi user có thể sử dụng (NULL = không giới hạn)

    private Boolean isNewUserOnly; // Chỉ áp dụng cho user mới (chưa có đơn hàng nào)

    private String status;

    // Áp dụng cho category cụ thể (optional)
    private String categoryId; // NULL = áp dụng cho tất cả

    @Indexed(unique = true)
    private String code; // Mã giảm giá để người dùng nhập

    public enum ApplicableFor {
        ORDER,
        SHIPPING
    }

    public enum Issuer {
        STORE,
        PLATFORM 
    }


    public enum PromotionType {
        PERCENTAGE,
        FIXED_AMOUNT
    }

    public enum DiscountType {
        ORDER,
        CATEGORY
    }

    public enum PromotionStatus {
        ACTIVE,
        INACTIVE,
        DELETED
    }   
}



