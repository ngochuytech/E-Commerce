package com.example.e_commerce_techshop.components;

import com.example.e_commerce_techshop.exceptions.JwtAuthenticationException;
import com.example.e_commerce_techshop.models.Token;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationDate;

    private final TokenRepository tokenRepository;

    public String generateToken(Authentication authentication){
        String username = authentication.getName();

        Date currentDate = new Date();

        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key())
                .compact();
    }

    public String generateToken(User user){
        String username = user.getEmail();

        Date currentDate = new Date();

        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key())
                .compact();
    }

    private SecretKey key(){
        byte[] bytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(bytes);
    }

    // get username from JWT token
    public String getUsername(String token){
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("JWT token đã hết hạn", e);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("JWT token không hợp lệ", e);
        } catch (Exception e) {
            throw new JwtAuthenticationException("Lỗi khi xử lý JWT token", e);
        }
    }

    // validate JWT token
    public boolean validateToken(String token, User user){
        try {
            String subject = extractClaim(token, Claims::getSubject);
            // Subject là email

            // Kiểm tra token tồn tại trong DB ko ?
            Token existingToken = tokenRepository.findByToken(token);
            if(existingToken == null || existingToken.isRevoked() ||!user.getIsActive()) {
                return false;
            }
            return (subject.equals(user.getUsername())) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("JWT token đã hết hạn", e);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("JWT token không hợp lệ", e);
        } catch (Exception e) {
            throw new JwtAuthenticationException("Lỗi khi xác thực JWT token", e);
        }
    }
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()  // Khởi tạo JwtParserBuilder
                    .verifyWith(key())  // Sử dụng verifyWith() để thiết lập signing key
                    .build()  // Xây dựng JwtParser
                    .parseSignedClaims(token)  // Phân tích token đã ký
                    .getPayload();  // Lấy phần body của JWT, chứa claims
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException("JWT token đã hết hạn", e);
        } catch (JwtException e) {
            throw new JwtAuthenticationException("JWT token không hợp lệ", e);
        } catch (Exception e) {
            throw new JwtAuthenticationException("Lỗi khi xử lý JWT token", e);
        }
    }


    public  <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = this.extractClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }
}
