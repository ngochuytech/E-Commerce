package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    User findByResetPasswordToken(String token);

    User findByVerificationCode(String code);
}
