package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, String> {
    Cart findByUserId(String userId);
}
