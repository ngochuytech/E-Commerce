package com.example.e_commerce_techshop.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleCodeRequest {
    @JsonProperty("code")
    private String code;

    @JsonProperty("redirectUri")
    private String redirectUri;
}

