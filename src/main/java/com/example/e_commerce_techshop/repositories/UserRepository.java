package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    User findByResetPasswordToken(String token);

    User findByVerificationCode(String code);
}
