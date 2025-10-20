package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

enum StoreStatus {
    PENDING, APPROVED, REJECTED, DELETED
}

@Document(collection = "stores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Store extends BaseEntity{
    @Id
    private String id;

    private String name;

    private String description;

    private String logoUrl;

    private String banner_url;

    private String status; // PENDING, APPROVED, REJECTED, DELETED

    @DBRef
    private User owner;

    private Address address;

    public enum StoreStatus {
        PENDING,
        APPROVED,
        REJECTED,
        DELETED
    }

    public static boolean isValidStatus(String status) {
        try {
            StoreStatus.valueOf(status.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Get valid status values as string array
    public static String[] getValidStatuses() {
        StoreStatus[] statuses = StoreStatus.values();
        String[] statusStrings = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            statusStrings[i] = statuses[i].name();
        }
        return statusStrings;
    }
}
