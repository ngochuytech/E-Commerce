package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.UserWallet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserWalletRepository extends MongoRepository<UserWallet, String> {
    Optional<UserWallet> findByUserId(String userId);
}
