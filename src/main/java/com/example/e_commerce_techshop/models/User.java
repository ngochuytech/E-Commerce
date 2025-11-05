package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Document(collection = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String phone;

    private String password;

    private String fullName;

    private String googleId;

    private LocalDateTime dateOfBirth;

    private String avatar;

    private Boolean enable;

    private String verificationCode;

    private String resetPasswordToken;

    private List<Address> address;

    private List<String> roles;

    // Ban
    private Boolean isActive;

    private String banReason;

    private LocalDateTime bannedAt;

    private LocalDateTime bannedUntil; // (null = vĩnh viễn)

    private String bannedBy;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return this.enable;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Kiểm tra có bị chặn không
        if (isActive == null || !isActive) {
            return false;
        }

        // Kiểm tra chặn tạm thời đã hết hạn chưa
        if (bannedUntil != null && LocalDateTime.now().isAfter(bannedUntil)) {
            return true; // Đã hết hạn chặn
        }

        // Nếu bannedUntil == null và isActive = false → chặn vĩnh viễn
        return bannedUntil == null || LocalDateTime.now().isBefore(bannedUntil);
    }
}
