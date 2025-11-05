package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Page<Order> findByBuyerIdAndStatus(String buyerId, String status, Pageable pageable);

    Page<Order> findByBuyerId(String buyerId, Pageable pageable);

    Optional<Order> findByIdAndBuyerId(String orderId, String buyerId);
    
    long countByBuyerIdAndStatus(String buyerId, String status);
    
    long countByBuyerId(String buyerId);
    
    List<Order> findByBuyerId(String buyerId);
    
    List<Order> findByStoreId(String storeId);
    
    List<Order> findByStoreIdAndStatus(String storeId, String status);
    
    long countByStoreIdAndStatus(String storeId, String status);
    
    @Query("{ 'store.$id': ?0, 'createdAt': { '$gte': ?1, '$lte': ?2 } }")
    List<Order> findByStoreIdAndDateRange(String storeId, LocalDateTime start, LocalDateTime end);
    
    Page<Order> findByStoreId(String storeId, Pageable pageable);
    
    Page<Order> findByStoreIdAndStatus(String storeId, String status, Pageable pageable);
    
    long countByStoreId(String storeId);
}
