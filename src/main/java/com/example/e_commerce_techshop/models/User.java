package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {
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

    // TODO: Thêm cột google_id vào database trước khi sử dụng
    // @Column(name = "google_id")
    // private String googleId;

    // TODO: Thêm cột date_of_birth vào database trước khi sử dụng (database có date, code dùng LocalDateTime)
    // @Column(name = "date_of_birth")
    // private LocalDateTime dateOfBirth;

    // TODO: Thêm cột avatar_url vào database trước khi sử dụng
    // @Column(name = "avatar_url", length = 255)
    // private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;



    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "role")
    private Integer role;

    // TODO: Thêm cột verification_code vào database trước khi sử dụng
    // @Column(name = "verification_code", length = 255)
    // private String verificationCode;

    // TODO: Thêm cột reset_password_token vào database trước khi sử dụng
    // @Column(name = "reset_password_token", length = 255)
    // private String resetPasswordToken;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Tạm thời return role name dựa trên role ID
        String roleName = switch (role) {
            case 1 -> "USER";
            case 2 -> "ADMIN";
            case 3 -> "SHOP";
            case 4 -> "SELLER";
            default -> "USER";
        };
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    @Override
    public boolean isEnabled(){
        return this.isActive;
    }
}
