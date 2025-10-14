package com.example.e_commerce_techshop.responses.user;

import java.time.LocalDateTime;

import com.example.e_commerce_techshop.models.User;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String id;
    
    private String email;

    private String phone;

    private String fullName;

    private LocalDateTime dateOfBirth;

    private String avatar;

    private AddressResponse address;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class AddressResponse {
        private String id;
        private String province;
        private String district;
        private String ward;
        private String homeAddress;
        private String suggestedName;
    }

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        AddressResponse addressResponse = AddressResponse.builder()
                .province(user.getAddress() != null ? user.getAddress().getProvince() : null)
                .ward(user.getAddress() != null ? user.getAddress().getWard() : null)
                .homeAddress(user.getAddress() != null ? user.getAddress().getHomeAddress() : null)
                .suggestedName(user.getAddress() != null ? user.getAddress().getSuggestedName() : null)
                .build();
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .avatar(user.getAvatarUrl())
                .address(addressResponse)
                .build();
    }

}
