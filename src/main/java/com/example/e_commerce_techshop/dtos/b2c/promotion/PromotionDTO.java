package com.example.e_commerce_techshop.dtos.b2c.promotion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDTO {
    @NotBlank(message = "Tiêu đề khuyến mãi không được để trống")
    private String title;

    @NotNull(message = "ID cửa hàng không được để trống")
    private String storeId;

    @NotBlank(message = "Loại khuyến mãi không được để trống")
    private String type; // PERCENTAGE, FIXED_AMOUNT

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // PRODUCT, ORDER, CATEGORY

    @Min(value = 0, message = "Giá trị giảm giá phải lớn hơn hoặc bằng 0")
    private Long discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private Long minOrderValue;

    @Min(value = 0, message = "Giá trị giảm giá tối đa phải lớn hơn hoặc bằng 0")
    private Long maxDiscountValue;

    private String status; // ACTIVE, INACTIVE, EXPIRED
}



