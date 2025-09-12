package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, String> {
}
