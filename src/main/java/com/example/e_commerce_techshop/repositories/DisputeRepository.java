package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends MongoRepository<Dispute, String> {

    // Tìm theo return request
    Optional<Dispute> findByReturnRequestId(String returnRequestId);
    
    // Tìm theo return request và dispute type
    Optional<Dispute> findByReturnRequestIdAndDisputeType(String returnRequestId, String disputeType);

    // Tìm theo order
    Optional<Dispute> findByOrderId(String orderId);

    // Tìm theo buyer
    Page<Dispute> findByBuyerId(String buyerId, Pageable pageable);
    
    List<Dispute> findByBuyerId(String buyerId);

    // Tìm theo store
    Page<Dispute> findByStoreId(String storeId, Pageable pageable);
    
    List<Dispute> findByStoreId(String storeId);

    // Tìm theo status (cho admin)
    Page<Dispute> findByStatus(String status, Pageable pageable);
    
    List<Dispute> findByStatus(String status);
    
    // Tìm theo dispute type
    Page<Dispute> findByDisputeType(String disputeType, Pageable pageable);
    
    // Tìm theo status và dispute type
    Page<Dispute> findByStatusAndDisputeType(String status, String disputeType, Pageable pageable);

    // Tìm tất cả dispute chưa giải quyết
    Page<Dispute> findByStatusIn(List<String> statuses, Pageable pageable);

    // Đếm theo status
    long countByStatus(String status);
    
    // Kiểm tra đã có dispute cho return request chưa
    boolean existsByReturnRequestId(String returnRequestId);
    
    // Kiểm tra đã có dispute cho return request với loại cụ thể chưa
    boolean existsByReturnRequestIdAndDisputeType(String returnRequestId, String disputeType);
}
