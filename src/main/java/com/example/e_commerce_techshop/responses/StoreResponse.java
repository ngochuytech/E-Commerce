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
    private String ownerId;
    private String ownerName;
    private String addressId;
    private String createdAt;
    private String updatedAt;

    public static StoreResponse fromStore(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .logoUrl(store.getLogo_url())
                .bannerUrl(store.getBanner_url())
                .status(store.getStatus())
                .ownerId(store.getOwner() != null ? store.getOwner().getId() : null)
                .ownerName(store.getOwner() != null ? store.getOwner().getFullName() : null)
                .addressId(store.getAddress() != null ? store.getAddress().getId() : null)
                .createdAt(store.getCreatedAt() != null ? store.getCreatedAt().toString() : null)
                .updatedAt(store.getUpdatedAt() != null ? store.getUpdatedAt().toString() : null)
                .build();
    }
}



