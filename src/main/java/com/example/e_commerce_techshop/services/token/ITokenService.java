package com.example.e_commerce_techshop.services.token;

import com.example.e_commerce_techshop.models.Token;
import com.example.e_commerce_techshop.models.User;

public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);

    Token refreshToken(String refreshToken, User user) throws Exception;

    Token findByRefreshToken(String refreshToken);

    void revokeToken(String refreshToken) throws Exception;
}
