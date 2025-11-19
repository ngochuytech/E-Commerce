package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.AdminRevenue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRevenueRepository extends MongoRepository<AdminRevenue, String> {
    List<AdminRevenue> findByStatus(String status);

    List<AdminRevenue> findByRevenueType(String revenueType);

    List<AdminRevenue> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Optional<AdminRevenue> findByOrderId(String orderId);

    @Query("{ 'status': 'COLLECTED', 'revenueType': 'SERVICE_FEE' }")
    List<AdminRevenue> findAllCollectedServiceFees();

    @Query(value = "{ 'status': ?0, 'revenueType': 'SERVICE_FEE' }", fields = "{ 'serviceFee': 1 }")
    List<AdminRevenue> findServiceFeesByStatus(String status);
}
