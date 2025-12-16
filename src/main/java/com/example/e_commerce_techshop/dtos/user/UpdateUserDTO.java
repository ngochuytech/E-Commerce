package com.example.e_commerce_techshop.dtos.user;

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
public class UpdateUserDTO {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Size(min = 10, max = 10, message = "The phone number must be exactly 10 digits")
    private String phone;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

}
