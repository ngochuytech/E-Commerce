package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.UserWithdrawalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserWithdrawalRequestRepository extends MongoRepository<UserWithdrawalRequest, String> {
    Page<UserWithdrawalRequest> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<UserWithdrawalRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<UserWithdrawalRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
