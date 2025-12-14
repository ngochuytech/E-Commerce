package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends MongoRepository<ReturnRequest, String> {

    // Tìm theo order
    Optional<ReturnRequest> findByOrderId(String orderId);

    // Tìm theo buyer
    Page<ReturnRequest> findByBuyerId(String buyerId, Pageable pageable);
    
    List<ReturnRequest> findByBuyerId(String buyerId);

    // Tìm theo store
    Page<ReturnRequest> findByStoreId(String storeId, Pageable pageable);
    
    List<ReturnRequest> findByStoreId(String storeId);

    // Tìm theo store và status
    Page<ReturnRequest> findByStoreIdAndStatus(String storeId, String status, Pageable pageable);
    
    List<ReturnRequest> findByStoreIdAndStatus(String storeId, String status);

    // Tìm theo buyer và status
    Page<ReturnRequest> findByBuyerIdAndStatus(String buyerId, String status, Pageable pageable);

    // Tìm theo status (cho admin)
    Page<ReturnRequest> findByStatus(String status, Pageable pageable);
    
    List<ReturnRequest> findByStatus(String status);

    // Đếm theo status
    long countByStatus(String status);
    
    long countByStoreIdAndStatus(String storeId, String status);
    
    long countByBuyerIdAndStatus(String buyerId, String status);

    // Kiểm tra đã có yêu cầu trả hàng cho order chưa
    boolean existsByOrderIdAndStatusNot(String orderId, String status);
    
    // Tìm các return request theo status và thời gian cập nhật (cho scheduled task)
    List<ReturnRequest> findByStatusAndUpdatedAtBefore(String status, LocalDateTime dateTime);
}
