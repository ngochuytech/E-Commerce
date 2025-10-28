package com.example.e_commerce_techshop.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String id;
    
    private String province;

    private String ward;

    private String homeAddress;

    private String suggestedName;

    private String phone;

    private boolean isDefault;
}