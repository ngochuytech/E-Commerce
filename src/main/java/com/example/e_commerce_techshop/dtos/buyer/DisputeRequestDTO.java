package com.example.e_commerce_techshop.dtos.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO cho buyer khiếu nại lên admin khi store từ chối trả hàng
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisputeRequestDTO {

    @NotBlank(message = "Nội dung khiếu nại không được để trống")
    @Size(max = 2000, message = "Nội dung khiếu nại không được quá 2000 ký tự")
    private String content;
}
