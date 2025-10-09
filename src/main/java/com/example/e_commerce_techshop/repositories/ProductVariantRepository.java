package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);

    List<ProductVariant> findByProduct_Category(String category);

    List<ProductVariant> findByProduct_CategoryAndProduct_Brand_Name(String category, String brand);

    List<ProductVariant> findByStockLessThan(Integer stock);

    List<ProductVariant> findByStockEquals(Integer stock);

    Page<ProductVariant> findByProductStoreId(String storeId, Pageable pageable);

}
