package com.example.e_commerce_techshop.dtos.user;

import java.time.LocalDate;

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
public class UpdateUserDTO {
    @NotBlank(message = "Password is required")

    private String phone;
    @NotBlank(message = "Full name is required")

    private String fullName;
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

}
