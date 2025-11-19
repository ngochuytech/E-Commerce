package com.example.e_commerce_techshop.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.e_commerce_techshop.models.OrderItem;

@Repository
public interface OrderItemRepository extends MongoRepository<OrderItem, String> {
    
    List<OrderItem> findByOrderId(String orderId);

    @Query("{'order.$id': {$in: ?0}}")
    List<OrderItem> findByOrderIdIn(List<String> orderIds);

}
