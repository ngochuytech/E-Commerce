package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryId(String categoryId);

    List<Product> findByCategoryIdAndBrandId(String categoryId, String brandId);
    
    List<Product> findByBrandId(String brandId);
    
    List<Product> findByStoreId(String storeId);

    List<Product> findByStatus(String status);

    List<Product> findByCategoryIdAndStatus(String categoryId, String status);
    
    List<Product> findByBrandIdAndStatus(String brandId, String status);
    
    List<Product> findByStoreIdAndStatus(String storeId, String status);

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryId(String categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndBrandId(String categoryId, String brandId, Pageable pageable);

    Page<Product> findByStoreId(String storeId, Pageable pageable);

    Page<Product> findByStoreIdAndStatus(String storeId, String status, Pageable pageable);

    long countByStatus(String status);

}
