package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Token;
import com.example.e_commerce_techshop.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {

    List<Token> findByUser(User user);

    Token findByRefreshToken(String token);

    Token findByToken(String token);

    long deleteByRefreshToken(String refreshToken);
}
