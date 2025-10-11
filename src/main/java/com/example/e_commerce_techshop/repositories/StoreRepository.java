package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends MongoRepository<Store, String> {
    List<Store> findByOwnerId(String ownerId);
    List<Store> findByStatus(String status);
    List<Store> findByNameContainingIgnoreCase(String name);
}
