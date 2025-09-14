package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);

    List<ProductVariant> findByProduct_Category(String category);

    List<ProductVariant> findByProduct_CategoryAndProduct_Brand_Name(String category, String brand);

}
