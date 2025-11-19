package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(String walletId, Pageable pageable);
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(String walletId);
}
