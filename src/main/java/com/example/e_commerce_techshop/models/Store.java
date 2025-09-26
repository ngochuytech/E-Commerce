package com.example.e_commerce_techshop.models;

import jakarta.persistence.*;
import lombok.*;

enum StoreStatus {
    PENDING, APPROVED, REJECTED, DELETED
}

@Entity
@Table(name = "stores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Store extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    private String description;

    private String logo_url;

    private String banner_url;

    @Column(name = "status", length = 50)
    private String status; // PENDING, APPROVED, REJECTED, DELETED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

    // Validation method for Store status
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
