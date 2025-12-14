package com.example.e_commerce_techshop.dtos.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * DTO cho yêu cầu trả hàng từ buyer
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnRequestDTO {

    @NotBlank(message = "Lý do trả hàng không được để trống")
    private String reason;

    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;

    @NotEmpty(message = "Vui lòng cung cấp ít nhất 1 hình ảnh minh chứng")
    @Size(min = 1, max = 5, message = "Số lượng hình ảnh minh chứng từ 1 đến 5")
    private List<String> evidenceImages;

    // Thông tin tài khoản ngân hàng để nhận tiền hoàn (bắt buộc nếu thanh toán COD)
    private String bankName;
    
    private String bankAccountNumber;
    
    private String bankAccountName;
}
