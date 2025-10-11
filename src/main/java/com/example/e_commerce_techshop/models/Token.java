package com.example.e_commerce_techshop.models;

import lombok.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Token {
    @Id
    private String id;

    private String token;

    private String refreshToken;

    private String tokenType;

    private LocalDateTime expirationDate;

    private LocalDateTime refreshExpirationDate;

    private boolean revoked;

    private boolean expired;

    private boolean isMobile;
    
    @DBRef
    private User user;
}
