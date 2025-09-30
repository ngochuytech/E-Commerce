package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByCartId(String cartId);
}
