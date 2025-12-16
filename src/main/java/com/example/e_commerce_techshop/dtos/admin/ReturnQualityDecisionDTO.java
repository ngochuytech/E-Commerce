package com.example.e_commerce_techshop.dtos.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for admin to decide on return quality dispute
 * When store disputes that returned goods have quality issues
 */
@Data
public class ReturnQualityDecisionDTO {
    
    /**
     * Decision: APPROVE_STORE (store wins - refund to store wallet) 
     * or REJECT_STORE (buyer wins - refund to buyer bank account)
     */
    @NotBlank(message = "Decision is required")
    private String decision; // APPROVE_STORE or REJECT_STORE
    
    /**
     * Admin's reason for the decision
     */
    @NotBlank(message = "Reason is required")
    private String reason;
}
