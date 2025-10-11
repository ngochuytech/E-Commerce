package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends MongoRepository<Brand, String> {
    Optional<Brand> findByName(String name);
    boolean existsByName(String name);
}
