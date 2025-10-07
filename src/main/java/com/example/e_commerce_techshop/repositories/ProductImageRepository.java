package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductImage;
import com.example.e_commerce_techshop.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    /**
     * Tìm tất cả ảnh theo product variant
     */
    List<ProductImage> findByProductVariant(ProductVariant productVariant);
    
    /**
     * Tìm tất cả ảnh theo product variant id
     */
    List<ProductImage> findByProductVariantId(String productVariantId);
    
    /**
     * Tìm ảnh chính của product variant
     */
    Optional<ProductImage> findByProductVariantAndIsPrimary(ProductVariant productVariant, Boolean isPrimary);
    
    /**
     * Tìm ảnh chính theo product variant id
     */
    Optional<ProductImage> findByProductVariantIdAndIsPrimary(String productVariantId, Boolean isPrimary);
    
    /**
     * Xóa tất cả ảnh của product variant
     */
    void deleteByProductVariant(ProductVariant productVariant);
    
    /**
     * Xóa tất cả ảnh theo product variant id
     */
    void deleteByProductVariantId(String productVariantId);
    
    /**
     * Đếm số lượng ảnh của product variant
     */
    long countByProductVariant(ProductVariant productVariant);
    
    /**
     * Đếm số lượng ảnh theo product variant id
     */
    long countByProductVariantId(String productVariantId);
}