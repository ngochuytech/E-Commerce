package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
}
