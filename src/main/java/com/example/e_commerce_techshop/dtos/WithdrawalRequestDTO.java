package com.example.e_commerce_techshop.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalRequestDTO {
    
    private BigDecimal amount;
    
    private String bankName;
    
    private String bankAccountNumber;
    
    private String bankAccountName;
    
    private String note;
}
