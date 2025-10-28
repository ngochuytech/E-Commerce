package com.example.e_commerce_techshop.dtos.b2c.promotion;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePromotionDTO {
    @NotBlank(message = "Tiêu đề khuyến mãi không được để trống")
    private String title;
    
    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Pattern(regexp = "^[A-Z0-9]{4,20}$", message = "Mã khuyến mãi phải từ 4-20 ký tự, chỉ chứa chữ in hoa và số")
    private String code; // Mã để người dùng nhập (unique)

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Pattern(regexp = "PERCENTAGE|FIXED_AMOUNT", message = "Loại giảm giá phải là PERCENTAGE hoặc FIXED_AMOUNT")
    private String type; // PERCENTAGE, FIXED_AMOUNT

    @NotBlank(message = "Áp dụng cho không được để trống")
    @Pattern(regexp = "ORDER|SHIPPING", message = "Áp dụng cho phải là ORDER hoặc SHIPPING")
    private String applicableFor; // ORDER, SHIPPING

    @NotBlank(message = "Loại discount không được để trống")
    @Pattern(regexp = "ORDER|CATEGORY", message = "Loại discount phải là ORDER hoặc CATEGORY")
    private String discountType; // ORDER, CATEGORY

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @Min(value = 1, message = "Giá trị giảm giá phải lớn hơn 0")
    private Long discountValue;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @Min(value = 0, message = "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private Long minOrderValue;

    @Min(value = 0, message = "Giá trị giảm giá tối đa phải lớn hơn hoặc bằng 0")
    private Long maxDiscountValue; // Chỉ áp dụng khi type = PERCENTAGE

    @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
    private Integer usageLimit; // NULL = không giới hạn

    private String categoryId;
}
