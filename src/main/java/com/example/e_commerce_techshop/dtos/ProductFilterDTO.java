package com.example.e_commerce_techshop.dtos;

import lombok.Data;

import java.util.Map;

@Data
public class ProductFilterDTO {
    private String category;

    private Map<String, String> attributes;
}
