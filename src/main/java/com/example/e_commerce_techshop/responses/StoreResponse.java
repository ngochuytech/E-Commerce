package com.example.e_commerce_techshop.responses;

import com.example.e_commerce_techshop.models.Store;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StoreResponse {
    private String id;
    private String name;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private String status;
    private Integer returnWarningCount;
    private String lastWarningMonth;

    private OwnerResponse owner;
    private AddressResponse address;
    private String createdAt;
    private String updatedAt;

    @Getter
    @Setter
    @Builder
    static class AddressResponse {
        private String province;
        private String ward;
        private String homeAddress;
        private String suggestedName;
    }

    @Getter
    @Setter
    @Builder
    static class OwnerResponse {
        private String id;
        private String fullName;
        private String email;
        private String phone;
    }

    public static StoreResponse fromStore(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .logoUrl(store.getLogoUrl())
                .bannerUrl(store.getBanner_url())
                .status(store.getStatus())
                .returnWarningCount(store.getReturnWarningCount())
                .lastWarningMonth(store.getLastWarningMonth())
                .owner(OwnerResponse.builder()
                        .id(store.getOwner() != null ? store.getOwner().getId() : null)
                        .fullName(store.getOwner() != null ? store.getOwner().getFullName() : null)
                        .email(store.getOwner() != null ? store.getOwner().getEmail() : null)
                        .phone(store.getOwner() != null ? store.getOwner().getPhone() : null)
                        .build())
                .address(AddressResponse.builder()
                        .province(store.getAddress() != null ? store.getAddress().getProvince() : null)
                        .ward(store.getAddress() != null ? store.getAddress().getWard() : null)
                        .homeAddress(store.getAddress() != null ? store.getAddress().getHomeAddress() : null)
                        .suggestedName(store.getAddress() != null ? store.getAddress().getSuggestedName() : null)
                        .build())
                .createdAt(store.getCreatedAt() != null ? store.getCreatedAt().toString() : null)
                .updatedAt(store.getUpdatedAt() != null ? store.getUpdatedAt().toString() : null)
                .build();
    }
}



