package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.UserTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserTransactionRepository extends MongoRepository<UserTransaction, String> {
    Page<UserTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId, Pageable pageable);
    List<UserTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId);
}
