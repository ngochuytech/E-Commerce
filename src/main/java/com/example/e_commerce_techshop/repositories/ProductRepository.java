package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Product;
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
}
