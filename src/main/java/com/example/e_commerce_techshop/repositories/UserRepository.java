package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
