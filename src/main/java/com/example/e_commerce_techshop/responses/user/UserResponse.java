package com.example.e_commerce_techshop.responses.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private boolean isActive;

    private boolean enable;

    private List<AddressResponse> address;

    private List<String> roles;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class AddressResponse {
        private String province;
        private String ward;
        private String homeAddress;
        private String suggestedName;
        private String phone;
        private boolean isDefault;
    }

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        List<AddressResponse> addressResponses = null;
        if (user.getAddress() != null) {
            addressResponses = user.getAddress().stream()
                    .map(address -> AddressResponse.builder()
                            .province(address.getProvince())
                            .ward(address.getWard())
                            .homeAddress(address.getHomeAddress())
                            .suggestedName(address.getSuggestedName())
                            .phone(address.getPhone())
                            .isDefault(address.isDefault())
                            .build())
                    .toList();
        }

        List<String> roleNames = new ArrayList<>();
        if(user.getRoles() != null) {
            roleNames = user.getRoles();
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .dateOfBirth(user.getDateOfBirth())
                .avatar(user.getAvatar())
                .address(addressResponses)
                .isActive(user.getIsActive())
                .enable(user.getEnable())
                .roles(roleNames)
                .build();
    }

}
