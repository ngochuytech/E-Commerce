package com.example.e_commerce_techshop.services.token;

import com.example.e_commerce_techshop.components.JwtTokenProvider;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.exceptions.ExpiredTokenException;
import com.example.e_commerce_techshop.models.Token;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService{

    private final JwtTokenProvider jwtTokenProvider;

    private final TokenRepository tokenRepository;

    private static final int MAX_TOKENS = 3;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.expiration-refresh-token}")
    private long expirationRefreshToken;

    @Override
    public Token addToken(User user, String token, boolean isMobileDevice) {
        List<Token> userTokens = tokenRepository.findByUser(user);
        int tokenCount = userTokens.size();
        // Số lượng token vượt quá giới hạn, xóa 1 token cũ
        if (tokenCount >= MAX_TOKENS) {
            // Ktra xem trong danh sách userTokens có tồn tại ít nhất 1 token
            // ko phải là thiết bị di động
            boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            if(hasNonMobileToken) {
                // Xóa token ko phải là token của mobile
                Token tokenToDelete = userTokens.stream()
                        .filter(userToken -> !userToken.isMobile())
                        .findFirst()
                        .orElse(userTokens.getFirst());
                tokenRepository.delete(tokenToDelete);
            }
            else {
                tokenRepository.delete(userTokens.getFirst());
            }
        }
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .build();
        newToken.setRefreshToken(UUID.randomUUID().toString());
        newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
        return tokenRepository.save(newToken);
    }

    @Override
    public Token refreshToken(String refreshToken, User user) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        if(existingToken == null){
            throw new DataNotFoundException("Không tìm thấy RefreshToken");
        }
        if(existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())){
            tokenRepository.delete(existingToken);
            throw new ExpiredTokenException("Refresh token đã hết hạn");
        }
        String token = jwtTokenProvider.generateToken(user);
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        existingToken.setExpirationDate(expirationDateTime);
        existingToken.setToken(token);
        existingToken.setRefreshToken(UUID.randomUUID().toString());
        existingToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
        return existingToken;
    }
}
