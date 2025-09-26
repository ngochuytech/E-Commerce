package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    /**
     * Tìm orders theo buyerId với phân trang
     */
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(String buyerId, Pageable pageable);
    
    /**
     * Tìm orders theo buyerId và status với phân trang
     */
    Page<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(String buyerId, String status, Pageable pageable);
    
    /**
     * Tìm order theo buyerId và orderId
     */
    Optional<Order> findByIdAndBuyerId(String orderId, String buyerId);
    
    /**
     * Đếm orders theo buyerId và status
     */
    long countByBuyerIdAndStatus(String buyerId, String status);
    
    /**
     * Đếm tổng orders theo buyerId
     */
    long countByBuyerId(String buyerId);
    
    /**
     * Lấy tất cả orders theo buyerId để đếm
     */
    List<Order> findByBuyerId(String buyerId);
    
    // B2C Methods - cho store owner
    /**
     * Tìm orders theo storeId
     */
    List<Order> findByStoreId(String storeId);
    
    /**
     * Tìm orders theo storeId và status
     */
    List<Order> findByStoreIdAndStatus(String storeId, String status);
    
    /**
     * Đếm orders theo storeId và status
     */
    long countByStoreIdAndStatus(String storeId, String status);
    
    /**
     * Tìm orders theo storeId và khoảng thời gian
     */
    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.createdAt BETWEEN :start AND :end")
    List<Order> findByStoreIdAndDateRange(@Param("storeId") String storeId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Tìm orders theo storeId với phân trang
     */
    Page<Order> findByStoreId(String storeId, Pageable pageable);
    
    /**
     * Tìm orders theo storeId và status với phân trang
     */
    Page<Order> findByStoreIdAndStatus(String storeId, String status, Pageable pageable);
    
    /**
     * Đếm orders theo storeId
     */
    long countByStoreId(String storeId);
}