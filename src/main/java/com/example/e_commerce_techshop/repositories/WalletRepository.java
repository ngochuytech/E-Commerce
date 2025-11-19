package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WalletRepository extends MongoRepository<Wallet, String> {
    Optional<Wallet> findByStoreId(String storeId);
}
