package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.AdminRevenue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRevenueRepository extends MongoRepository<AdminRevenue, String> {
    List<AdminRevenue> findByRevenueType(String revenueType);

    Page<AdminRevenue> findByRevenueType(String revenueType, Pageable pageable);

    Page<AdminRevenue> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<AdminRevenue> findByOrderId(String orderId);

    @Query("{ 'revenueType': 'SERVICE_FEE' }")
    List<AdminRevenue> findAllServiceFees();
}
