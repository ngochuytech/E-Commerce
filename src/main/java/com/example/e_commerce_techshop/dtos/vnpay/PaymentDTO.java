package com.example.e_commerce_techshop.dtos.vnpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentDTO {
    @JsonProperty("amount")
    private Long amount; // Số tiền cần thanh toán

    @JsonProperty("bankCode")
    private String bankCode; // Mã ngân hàng

    @JsonProperty("language")
    private String language; // Ngôn ngữ giao diện thanh toán (vd: "vn", "en")
}
