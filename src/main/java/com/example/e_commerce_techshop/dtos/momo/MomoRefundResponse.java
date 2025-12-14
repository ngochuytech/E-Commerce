package com.example.e_commerce_techshop.dtos.momo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoRefundResponse {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("transId")
    private Long transId;
    
    @JsonProperty("resultCode")
    private Integer resultCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("responseTime")
    private Long responseTime;
    
    /**
     * Kiểm tra xem hoàn tiền có thành công không
     * resultCode = 0 nghĩa là thành công
     */
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }
}
