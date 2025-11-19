package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Store;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends MongoRepository<Store, String> {
    List<Store> findByOwnerId(String ownerId);
    Optional<Store> findByIdAndOwnerId(String id, String ownerId);

    Page<Store> findByStatus(String status, Pageable pageable);
    Page<Store> findByOwnerId(String ownerId, Pageable pageable);

}
