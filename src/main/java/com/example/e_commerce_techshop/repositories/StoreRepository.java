package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, String> {
    List<Store> findByOwnerId(String ownerId);
    List<Store> findByStatus(String status);
    List<Store> findByNameContainingIgnoreCase(String name);
}
