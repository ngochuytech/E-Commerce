package com.example.e_commerce_techshop.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private BigDecimal platformCommission; // Hoa hồng sàn lấy (5% doanh thu shop tối đa 500k)

    private BigDecimal storeDiscountAmount; // Tiền giảm từ shop (shop chịu)

    private BigDecimal platformDiscountAmount; // Tiền giảm từ sàn (sàn chịu)

    private BigDecimal totalDiscountAmount; // Tổng tiền giảm giá

    private String paymentMethod;

    private String status;

    private String note;

    private boolean isRated;

    private boolean hasReturnRequest; // Đơn hàng có yêu cầu trả hàng không (không tính CLOSED)

    private String vnpTnxRef; // Mã tham chiếu giao dịch VNPAY (nếu có)

    private String momoTransId; // Mã giao dịch MoMo (transId khi thanh toán thành công)
    private String momoRefundOrderId; // Mã đơn hoàn tiền MoMo (RF_xxx) để check refund status
    private String paymentStatus; // Trạng thái thanh toán: UNPAID, PAID, FAILED, REFUNDED

    private String rejectReason; // Lý do từ chối đơn hàng (nếu có)

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

    private String refundStatus; // Trạng thái hoàn tiền: PENDING, PROCESSING, COMPLETED, FAILED
    private String refundTransactionId; // Mã giao dịch hoàn tiền
    private LocalDateTime refundRequestedAt; // Thời điểm yêu cầu hoàn tiền
    
    // Thông tin hoàn tiền thủ công (cho COD)
    private String manualRefundTransactionRef; // Mã giao dịch chuyển khoản thủ công
    private LocalDateTime manualRefundTransferredAt; // Thời điểm admin chuyển tiền
    private String manualRefundNote; // Ghi chú hoàn tiền thủ công
    private LocalDateTime refundCompletedAt; // Thời điểm hoàn tiền thành công

    public enum RefundStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, COMPLETED, RETURNING, RETURNED
    }

    public enum PaymentMethod {
        COD, MOMO, VNPAY
    }

    public enum PaymentStatus {
        UNPAID,    // Chưa thanh toán
        PAID,      // Đã thanh toán
        FAILED     // Thanh toán thất bại
    }
}
