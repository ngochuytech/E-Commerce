package com.example.e_commerce_techshop.services.refund;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.RefundRequest;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.RefundRequestRepository;
import com.example.e_commerce_techshop.services.momo.IMomoService;
import com.example.e_commerce_techshop.services.notification.INotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefundService implements IRefundService {
    private final OrderRepository orderRepository;
    private final INotificationService notificationService;
    private final RefundRequestRepository refundRequestRepository;
    private final IMomoService momoService;

    @Override
    @Transactional
    public void createRefundRequest(Order order) throws Exception {
        // Cập nhật trạng thái hoàn tiền
        order.setRefundStatus(Order.RefundStatus.PENDING.name());
        order.setRefundRequestedAt(LocalDateTime.now());

        orderRepository.save(order);

        // Gửi thông báo cho admin về yêu cầu hoàn tiền
        try {
            notificationService.createAdminNotification(
                    "Yêu cầu hoàn tiền mới",
                    String.format("Đơn hàng #%s cần hoàn tiền %,.0f đ cho khách hàng %s qua %s",
                            order.getId(),
                            order.getTotalPrice().doubleValue(),
                            order.getBuyer().getFullName(),
                            order.getPaymentMethod()),
                    Notification.NotificationType.REFUND_REQUEST.name(),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error creating admin notification: " + e.getMessage());
        }

        // Xử lý hoàn tiền theo phương thức thanh toán
        String paymentMethod = order.getPaymentMethod();

        if ("MOMO".equalsIgnoreCase(paymentMethod)) {
            // Hoàn tiền qua MoMo
            if (order.getMomoTransId() != null && !order.getMomoTransId().isEmpty()) {
                String description = String.format("Hoàn tiền đơn hàng #%s - %s", 
                    order.getId(), 
                    order.getStatus());
                processMomoRefund(order.getId(), order.getTotalPrice(), order.getMomoTransId(), description);
            } else {
                throw new IllegalArgumentException("Không tìm thấy mã giao dịch MoMo để hoàn tiền");
            }
        } else {
            throw new IllegalArgumentException("Phương thức thanh toán không hỗ trợ hoàn tiền tự động: " + paymentMethod);
        }
    }

    @Override
    @Transactional
    public String processMomoRefund(String orderId, BigDecimal amount, String momoTransId, String description) 
            throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        try {
            // Cập nhật trạng thái đang xử lý
            order.setRefundStatus(Order.RefundStatus.PROCESSING.name());
            orderRepository.save(order);

            // Gọi API MoMo để hoàn tiền
            Long transIdLong = Long.parseLong(momoTransId);
            Long amountLong = amount.longValue();
            
            String momoResponse = momoService.refundPayment(transIdLong, amountLong, description);
            
            // Parse response từ MoMo
            org.cloudinary.json.JSONObject json = new org.cloudinary.json.JSONObject(momoResponse);
            int resultCode = json.getInt("resultCode");
            String refundTransId = json.optString("transId", "");
            
            if (resultCode == 0) {
                // Hoàn tiền thành công
                order.setRefundStatus(Order.RefundStatus.COMPLETED.name());
                order.setRefundCompletedAt(LocalDateTime.now());
                order.setRefundTransactionId(refundTransId);
                orderRepository.save(order);

                // Tạo RefundRequest record
                RefundRequest refundRequest = RefundRequest.builder()
                        .order(order)
                        .buyer(order.getBuyer())
                        .refundAmount(amount)
                        .paymentMethod("MOMO")
                        .refundTransactionId(refundTransId)
                        .status(RefundRequest.RefundStatus.COMPLETED.name())
                        .build();
                refundRequestRepository.save(refundRequest);

                // Thông báo cho khách hàng
                notificationService.createUserNotification(
                        order.getBuyer().getId(),
                        "Hoàn tiền thành công",
                        String.format(
                                "Đơn hàng #%s đã được hoàn tiền %,.0f đ về ví MoMo của bạn. Mã GD: %s",
                                orderId, amount.doubleValue(), refundTransId),
                        orderId);

                System.out.println("MoMo refund successful for order: " + orderId + ", TransId: " + refundTransId);
                return refundTransId;
                
            } else {
                // Hoàn tiền thất bại
                String errorMessage = json.optString("message", "Unknown error");
                order.setRefundStatus(Order.RefundStatus.FAILED.name());
                orderRepository.save(order);

                // Thông báo cho admin
                notificationService.createAdminNotification(
                        "Hoàn tiền MoMo thất bại",
                        String.format("Đơn hàng #%s: Hoàn tiền thất bại - %s (ResultCode: %d)",
                                orderId, errorMessage, resultCode),
                        Notification.NotificationType.REFUND_REQUEST.name(),
                        orderId);

                throw new RuntimeException("MoMo refund failed: " + errorMessage + " (Code: " + resultCode + ")");
            }

        } catch (Exception e) {
            order.setRefundStatus(Order.RefundStatus.FAILED.name());
            orderRepository.save(order);

            // Thông báo lỗi cho admin
            notificationService.createAdminNotification(
                    "Lỗi hoàn tiền MoMo",
                    String.format("Đơn hàng #%s: Lỗi khi xử lý hoàn tiền - %s", orderId, e.getMessage()),
                    Notification.NotificationType.REFUND_REQUEST.name(),
                    orderId);

            System.err.println("Error processing MoMo refund: " + e.getMessage());
            throw new RuntimeException("Không thể xử lý hoàn tiền qua MoMo: " + e.getMessage());
        }
    }

    @Override
    public String checkRefundStatus(String orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        return order.getRefundStatus() != null ? order.getRefundStatus() : "NO_REFUND";
    }
}
