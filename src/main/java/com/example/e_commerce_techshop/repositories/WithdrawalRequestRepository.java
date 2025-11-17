package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.WithdrawalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WithdrawalRequestRepository extends MongoRepository<WithdrawalRequest, String> {
    Page<WithdrawalRequest> findByStoreIdOrderByCreatedAtDesc(String storeId, Pageable pageable);
    List<WithdrawalRequest> findByStoreIdOrderByCreatedAtDesc(String storeId);
    Page<WithdrawalRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
