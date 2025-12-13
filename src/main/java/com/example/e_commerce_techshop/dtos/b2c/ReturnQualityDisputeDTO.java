package com.example.e_commerce_techshop.dtos.b2c;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

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

    @NotEmpty(message = "Vui lòng cung cấp ít nhất 1 hình ảnh minh chứng")
    @Size(min = 1, max = 10, message = "Số lượng hình ảnh minh chứng từ 1 đến 10")
    private List<String> evidenceImages;
}
