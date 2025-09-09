package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "password", length = 255)
    private String password
            ;
    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;


}
