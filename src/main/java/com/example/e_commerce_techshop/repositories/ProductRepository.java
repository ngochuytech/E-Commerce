package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategory(String category);

    List<Product> findByCategoryAndBrand_Name(String category, String name);
}
