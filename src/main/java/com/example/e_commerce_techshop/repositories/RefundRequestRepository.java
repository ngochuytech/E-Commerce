package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.RefundRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRequestRepository extends MongoRepository<RefundRequest, String> {
    
    // Tìm theo trạng thái
    Page<RefundRequest> findByStatus(String status, Pageable pageable);
    
    // Tìm tất cả với phân trang
    Page<RefundRequest> findAll(Pageable pageable);
    
    // Tìm theo order ID
    @Query("{ 'order.$id': { $oid: ?0 } }")
    Optional<RefundRequest> findByOrderId(String orderId);
    
    // Đếm theo trạng thái
    long countByStatus(String status);
    
    // Tìm theo buyer
    @Query("{ 'buyer.$id': { $oid: ?0 } }")
    Page<RefundRequest> findByBuyerId(String buyerId, Pageable pageable);
}
