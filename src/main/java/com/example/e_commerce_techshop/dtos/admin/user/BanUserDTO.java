package com.example.e_commerce_techshop.dtos.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BanUserDTO {
    
    @NotBlank(message = "User ID không được để trống")
    private String userId;
    
    @NotBlank(message = "Lý do chặn không được để trống")
    private String reason;
    
    @NotNull(message = "Loại chặn không được để trống")
    private BanType banType; // TEMPORARY, PERMANENT
    
    private Integer durationDays; // Số ngày chặn (nếu TEMPORARY)
    
    public enum BanType {
        TEMPORARY,
        PERMANENT
    }
}