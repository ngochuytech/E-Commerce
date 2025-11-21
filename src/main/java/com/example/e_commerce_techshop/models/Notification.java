package com.example.e_commerce_techshop.models;

import lombok.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    @DBRef
    private User user;

    @DBRef
    private Store store;

    private String title;

    private String message;

    private Boolean isRead;

    @CreatedDate
    private LocalDateTime createdAt;


}
