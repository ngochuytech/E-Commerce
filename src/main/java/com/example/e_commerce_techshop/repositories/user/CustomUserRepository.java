package com.example.e_commerce_techshop.repositories.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.e_commerce_techshop.models.User;

public interface CustomUserRepository {
    Page<User> findAllByFilters(String userName, String userEmail, String userPhone, Pageable pageable);
}
