package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    Page<ProductVariant> findByCategoryNameAndStatus(String categoryName, String status, Pageable pageable);

    Page<ProductVariant> findByCategoryNameAndBrandNameAndStatus(String category, String brand, String status, Pageable pageable);

    Page<ProductVariant> findByStoreIdAndStatus(String storeId, String status, Pageable pageable);

    Page<ProductVariant> findByStatus(String status, Pageable pageable);

    Page<ProductVariant> findByStoreId(String storeId, Pageable pageable);

    Page<ProductVariant> findByProductIdAndStatus(String productId, String status, Pageable pageable);

    @Query("{'name': {$regex: ?0, $options: 'i'}, 'status': ?1}")
    Page<ProductVariant> searchByNameAndStatus(String name, String status, Pageable pageable);

    long countByStatus(String status);

    long countByStoreIdAndStatus(String storeId, String status);

    List<ProductVariant> findByStoreIdAndStatus(String storeId, String status);
}
