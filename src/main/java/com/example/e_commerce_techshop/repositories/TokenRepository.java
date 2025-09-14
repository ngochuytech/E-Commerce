package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Token;
import com.example.e_commerce_techshop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, String> {

    List<Token> findByUser(User user);
    Token findByRefreshToken(String token);
    Token findByToken(String token);
}
