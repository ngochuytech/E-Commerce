package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    // Find orders by store
    List<Order> findByStoreId(String storeId);
    
    // Find orders by status
    List<Order> findByStatus(String status);
    
    // Find orders by store and status
    List<Order> findByStoreIdAndStatus(String storeId, String status);
    
    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByStoreIdAndDateRange(@Param("storeId") String storeId, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Count orders by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.storeId = :storeId AND o.status = :status")
    Long countByStoreIdAndStatus(@Param("storeId") String storeId, @Param("status") String status);
    
    // Find recent orders
    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByStoreId(@Param("storeId") String storeId);
}



