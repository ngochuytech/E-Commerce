package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.OrderItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends MongoRepository<OrderItem, String> {
    
    List<OrderItem> findByOrderId(String orderId);
    
    long countByOrderId(String orderId);
}
