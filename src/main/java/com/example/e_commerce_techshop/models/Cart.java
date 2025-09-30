package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private String id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
