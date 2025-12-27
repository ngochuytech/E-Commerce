package com.example.e_commerce_techshop.dtos.b2c;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO cho store khiếu nại hàng trả về có vấn đề
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnQualityDisputeDTO {

    @NotBlank(message = "Lý do khiếu nại không được để trống")
    private String reason;

    @Size(max = 2000, message = "Mô tả không được quá 2000 ký tự")
    private String description;
}
