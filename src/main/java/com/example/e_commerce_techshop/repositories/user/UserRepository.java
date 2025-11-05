package com.example.e_commerce_techshop.repositories.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.e_commerce_techshop.models.User;

public interface UserRepository extends MongoRepository<User, String>, CustomUserRepository {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    User findByResetPasswordToken(String token);

    User findByVerificationCode(String code);

    List<User> findByIsActiveFalseAndBannedUntilNotNull();

}
