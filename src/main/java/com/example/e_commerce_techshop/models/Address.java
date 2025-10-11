package com.example.e_commerce_techshop.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String province;

    private String district;

    private String ward;

    private String homeAddress;

    private String suggestedName;
}