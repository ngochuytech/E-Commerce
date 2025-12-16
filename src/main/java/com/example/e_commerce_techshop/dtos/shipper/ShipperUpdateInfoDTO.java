package com.example.e_commerce_techshop.dtos.shipper;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipperUpdateInfoDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Size(min = 10, max = 10, message = "The phone number must be exactly 10 digits")
    private String phone;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;
}
