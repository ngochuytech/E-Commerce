package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    // TODO: Uncomment khi đã thêm cột reset_password_token vào database
    // User findByResetPasswordToken(String token);

    // TODO: Uncomment khi đã thêm cột verification_code vào database
    // User findByVerificationCode(String code);
}
