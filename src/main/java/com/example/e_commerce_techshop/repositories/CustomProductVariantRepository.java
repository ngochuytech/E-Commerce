package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomProductVariantRepository {
    Page<ProductVariant> searchByNameExcludingBannedStores(String name, String status, Pageable pageable);
    Page<ProductVariant> findByStatusExcludingBannedStores(String status, Pageable pageable);
    Page<ProductVariant> findByCategoryNameAndStatusExcludingBannedStores(String categoryName, String status, Pageable pageable);
    Page<ProductVariant> findByCategoryNameAndBrandNameAndStatusExcludingBannedStores(String categoryName, String brandName, String status, Pageable pageable);
}
