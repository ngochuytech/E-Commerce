package com.example.e_commerce_techshop.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.e_commerce_techshop.models.Shipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentResponse {
    private String id;
    private String shipperName;
    private OrderResponse order;
    private StoreResponse store;
    private AddressResponse fromAddress;
    private AddressResponse toAddress;
    private BigDecimal shippingFee;
    private String status;
    private LocalDateTime expectedDeliveryDate;
    private List<String> history;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class OrderResponse {
        private String id;
        private String totalPrice;
        private String paymentMethod;
        private String paymentStatus;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class StoreResponse {
        private String id;
        private String name;
        private String logo;
        private String province;
        private String ward;
        private String homeAddress;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class AddressResponse {
        private String province;
        private String ward;
        private String homeAddress;
        private String suggestedName;
    }

    public static ShipmentResponse fromShipment(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .shipperName(shipment.getCarrier() != null && shipment.getCarrier().getFullName() != null ? shipment.getCarrier().getFullName() : null)
                .order(OrderResponse.builder()
                        .id(shipment.getOrder().getId())
                        .totalPrice(shipment.getOrder().getTotalPrice().toString())
                        .paymentMethod(shipment.getOrder().getPaymentMethod())
                        .paymentStatus(shipment.getOrder().getPaymentStatus())
                        .build())
                .store(StoreResponse.builder()
                        .id(shipment.getOrder().getStore().getId())
                        .name(shipment.getOrder().getStore().getName())
                        .logo(shipment.getOrder().getStore().getLogoUrl())
                        .province(shipment.getOrder().getStore().getAddress().getProvince())
                        .ward(shipment.getOrder().getStore().getAddress().getWard())
                        .homeAddress(shipment.getOrder().getStore().getAddress().getHomeAddress())
                        .build())
                .fromAddress(AddressResponse.builder()
                        .province(shipment.getFromAddress().getProvince())
                        .ward(shipment.getFromAddress().getWard())
                        .homeAddress(shipment.getFromAddress().getHomeAddress())
                        .suggestedName(shipment.getFromAddress().getSuggestedName())
                        .build())
                .toAddress(AddressResponse.builder()
                        .province(shipment.getToAddress().getProvince())
                        .ward(shipment.getToAddress().getWard())
                        .homeAddress(shipment.getToAddress().getHomeAddress())
                        .suggestedName(shipment.getToAddress().getSuggestedName())
                        .build())
                .shippingFee(shipment.getShippingFee())
                .status(shipment.getStatus())
                .history(shipment.getHistory() != null ? shipment.getHistory() : List.of())
                .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}
