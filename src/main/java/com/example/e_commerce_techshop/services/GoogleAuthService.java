package com.example.e_commerce_techshop.services;

import com.example.e_commerce_techshop.dtos.GoogleCodeRequest;
import com.example.e_commerce_techshop.models.Role;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private String scope = "openid email profile";

    public String generateAuthUrl(){
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
    }

    public User loginWithGoogle(GoogleCodeRequest request) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Gửi code sang Google để đổi lấy token
        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        Map<String, String> body = Map.of(
                "code", request.getCode(),
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", request.getRedirectUri(),
                "grant_type", "authorization_code"
        );

        JsonNode tokenResponse = restTemplate.postForObject(tokenEndpoint, body, JsonNode.class);

        if (tokenResponse == null || !tokenResponse.has("id_token")) {
            throw new IllegalArgumentException("Invalid token response from Google");
        }

        String idToken = tokenResponse.get("id_token").asText();

        // 2. Gọi Google API để lấy thông tin user từ access_token
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";
        String accessToken = tokenResponse.get("access_token").asText();

        JsonNode userInfo = restTemplate.getForObject(userInfoEndpoint + "?access_token=" + accessToken, JsonNode.class);

        if (userInfo == null || !userInfo.has("email")) {
            throw new IllegalArgumentException("Cannot fetch user info from Google");
        }

        String googleId = userInfo.get("sub").asText();
        String email = userInfo.get("email").asText();
        String name = userInfo.get("name").asText();
        String picture = userInfo.get("picture").asText();

        // 3. Lưu hoặc cập nhật vào DB
        User user = userRepository.findByEmail(email).orElse(
                User.builder()
                        .googleId(googleId)
                        .email(email)
                        .fullName(name)
                        .avatar(picture)
                        .isActive(true)
                        .enable(true)
                        .roles(List.of(Role.USER)) // User
                        .build()
        );

        // Nếu user đã tồn tại thì update info
        user.setGoogleId(googleId);
        if (user.getAvatar() == null)
            user.setAvatar(picture);
        return userRepository.save(user);
    }
}
