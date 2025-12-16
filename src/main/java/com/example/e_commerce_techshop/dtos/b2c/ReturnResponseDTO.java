package com.example.e_commerce_techshop.dtos.b2c;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * DTO cho store phản hồi yêu cầu trả hàng
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnResponseDTO {

    private boolean approved;

    @Size(max = 1000, message = "Lý do không được quá 1000 ký tự")
    private String reason;

    private List<String> evidenceMedia;
}
