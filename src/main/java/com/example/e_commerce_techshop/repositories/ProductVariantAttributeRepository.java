package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ProductVariantAttribute;
import com.example.e_commerce_techshop.models.ProductVariantAttributeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute, ProductVariantAttributeId> {
    List<ProductVariantAttribute> findByProductVariant(ProductVariant productVariant);
}
