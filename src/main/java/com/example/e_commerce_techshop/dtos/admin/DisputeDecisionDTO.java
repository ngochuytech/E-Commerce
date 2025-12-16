package com.example.e_commerce_techshop.dtos.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho admin quyết định tranh chấp
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisputeDecisionDTO {

    @NotBlank(message = "Quyết định không được để trống")
    private String decision; // APPROVE_RETURN hoặc REJECT_RETURN

    @NotBlank(message = "Lý do quyết định không được để trống")
    @Size(max = 2000, message = "Lý do quyết định không được quá 2000 ký tự")
    private String reason;
}
