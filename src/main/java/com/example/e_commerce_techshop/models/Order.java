package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order extends BaseEntity {
    @Id
    private String id;

    private BigDecimal totalPrice; // Giá cuối cùng khách phải thanh toán

    private BigDecimal productPrice; // Giá sản phẩm (tổng)

    private BigDecimal shippingFee; // Phí ship

    private BigDecimal serviceFee; // Phí dịch vụ (cố định: 5000đ)

    private String paymentMethod;

    private String status;
    
    private String note;

    private boolean isRated;

    private String vnpTnxRef; // Mã tham chiếu giao dịch VNPAY (nếu có)

    @DBRef
    private List<OrderItem> orderItems;

    @DBRef
    private User buyer;

    @DBRef
    private Store store;

    private String phone; 

    @DBRef
    private List<Promotion> promotions;

    private Address address;

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
    }
}

