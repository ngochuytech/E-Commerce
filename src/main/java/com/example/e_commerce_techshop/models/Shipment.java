package com.example.e_commerce_techshop.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "shipments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Shipment extends BaseEntity {
    @Id
    private String id;

    @DBRef
    private Order order;

    @DBRef
    private Store store;

    private Address fromAddress;

    private Address toAddress;

    private BigDecimal shippingFee;

    @DBRef
    private User carrier;

    private boolean isReturnShipment;

    private String status;

    private List<String> history;

    // Ngày dự kiến giao hàng
    private LocalDateTime expectedDeliveryDate;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Address {
        private String province;
        private String ward;
        private String homeAddress;
        private String suggestedName;
    }

    public enum ShipmentStatus {
        READY_TO_PICK, PICKING, PICKED, SHIPPING, DELIVERED, DELIVERED_FAIL, RETURNING, RETURNED
    }

}
