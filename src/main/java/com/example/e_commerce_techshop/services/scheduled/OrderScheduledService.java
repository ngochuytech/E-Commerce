package com.example.e_commerce_techshop.services.scheduled;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.models.AdminRevenue;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.OrderItem;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.repositories.AdminRevenueRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ReturnRequestRepository;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.refund.IRefundService;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderScheduledService {

    private final OrderRepository orderRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final INotificationService notificationService;
    private final IWalletService walletService;
    private final IRefundService refundService;
    private final com.example.e_commerce_techshop.repositories.OrderItemRepository orderItemRepository;
    private final com.example.e_commerce_techshop.repositories.ProductVariantRepository productVariantRepository;
    private final AdminRevenueRepository adminRevenueRepository;

    /**
     * Tự động xác nhận hoàn thành đơn hàng sau 7 ngày kể từ khi giao hàng thành công
     * Chạy mỗi ngày lúc 00:00 (nửa đêm)
     * 
     * Flow: DELIVERED (đã giao) -> COMPLETED (hoàn thành) sau 7 ngày không có xác nhận từ khách
     */
    @Scheduled(cron = "0 0 0 * * *") // Chạy lúc 00:00 mỗi ngày
    @Transactional
    public void autoCompleteDeliveredOrders() {
        log.info("=== [OrderScheduledService] Bắt đầu kiểm tra đơn hàng DELIVERED quá 7 ngày ===");

        try {
            // Tìm các đơn hàng đã giao (DELIVERED) quá 7 ngày
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Order> deliveredOrders = orderRepository.findByStatusAndUpdatedAtBefore(
                    Order.OrderStatus.DELIVERED.name(), sevenDaysAgo);

            if (deliveredOrders.isEmpty()) {
                log.info("[OrderScheduledService] Không có đơn hàng nào cần tự động hoàn thành");
                return;
            }

            log.info("[OrderScheduledService] Tìm thấy {} đơn hàng cần tự động hoàn thành", deliveredOrders.size());

            int successCount = 0;
            int failCount = 0;

            for (Order order : deliveredOrders) {
                try {
                    // Cập nhật trạng thái sang COMPLETED
                    order.setStatus(Order.OrderStatus.COMPLETED.name());
                    orderRepository.save(order);

                    // Chuyển tiền từ pendingAmount sang balance
                    try {
                        // Tính doanh thu từ sản phẩm (sau khi trừ discount shop chịu)
                        BigDecimal productRevenue = order.getProductPrice()
                                .subtract(order.getStoreDiscountAmount() != null ? order.getStoreDiscountAmount() : BigDecimal.ZERO);
                        
                        // Sàn lấy 5% hoa hồng từ doanh thu sản phẩm, tối đa 500,000đ
                        BigDecimal platformCommission = productRevenue.multiply(BigDecimal.valueOf(0.05));
                        BigDecimal maxCommission = BigDecimal.valueOf(500000);
                        platformCommission = platformCommission.min(maxCommission);
                        
                        // Shop nhận doanh thu trừ đi hoa hồng + phí ship
                        BigDecimal storeRevenue = productRevenue.subtract(platformCommission)
                                .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
                        
                        // Chuyển từ pending sang balance (thay vì cộng thẳng)
                        walletService.transferPendingToBalance(
                                order.getStore().getId(),
                                order.getId(),
                                storeRevenue,
                                String.format("Thanh toán đơn hàng #%s (tự động hoàn thành sau 7 ngày)", order.getId())
                        );
                        log.info("[OrderScheduledService] Đã chuyển {} từ pending sang balance cho shop {} đơn #{}", 
                                storeRevenue, order.getStore().getId(), order.getId());
                        
                        // Lưu hoa hồng sàn vào AdminRevenue để thống kê
                        try {
                            AdminRevenue platformCommissionRevenue = AdminRevenue.builder()
                                    .order(order)
                                    .amount(platformCommission)
                                    .revenueType(AdminRevenue.RevenueType.PLATFORM_COMMISSION.name())
                                    .description(String.format("Hoa hồng 5%% từ đơn hàng #%s (tự động)", order.getId()))
                                    .build();
                            adminRevenueRepository.save(platformCommissionRevenue);
                            log.info("[OrderScheduledService] Đã lưu hoa hồng {} vào AdminRevenue cho đơn #{}", 
                                    platformCommission, order.getId());
                        } catch (Exception ex) {
                            log.error("[OrderScheduledService] Lỗi lưu hoa hồng vào AdminRevenue: {}", ex.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("[OrderScheduledService] Lỗi chuyển pending sang balance: {}", e.getMessage());
                    }

                    // Thông báo cho khách hàng
                    try {
                        notificationService.createUserNotification(order.getBuyer().getId(),
                                "Đơn hàng đã tự động hoàn thành",
                                String.format("Đơn hàng #%s đã được tự động xác nhận hoàn thành sau 7 ngày giao hàng. " +
                                        "Cảm ơn bạn đã mua sắm!", order.getId()),
                                order.getId());
                    } catch (Exception e) {
                        log.warn("[OrderScheduledService] Lỗi gửi thông báo cho buyer: {}", e.getMessage());
                    }

                    // Thông báo cho shop
                    try {
                        notificationService.createStoreNotification(order.getStore().getId(),
                                "Đơn hàng đã tự động hoàn thành",
                                String.format("Đơn hàng #%s đã được hệ thống tự động xác nhận hoàn thành sau 7 ngày giao hàng.",
                                        order.getId()),
                                order.getId());
                    } catch (Exception e) {
                        log.warn("[OrderScheduledService] Lỗi gửi thông báo cho store: {}", e.getMessage());
                    }

                    successCount++;
                    log.info("[OrderScheduledService] Đã hoàn thành đơn hàng #{}", order.getId());

                } catch (Exception e) {
                    failCount++;
                    log.error("[OrderScheduledService] Lỗi khi hoàn thành đơn hàng #{}: {}", 
                            order.getId(), e.getMessage());
                }
            }

            log.info("=== [OrderScheduledService] Kết thúc: {} thành công, {} thất bại ===", successCount, failCount);

        } catch (Exception e) {
            log.error("[OrderScheduledService] Lỗi khi chạy scheduled task: {}", e.getMessage(), e);
        }
    }

    /**
     * Tự động xác nhận hoàn tiền cho khách khi store không phản hồi trong 2 ngày
     * Chạy mỗi ngày lúc 01:00 sáng
     * 
     * Flow: RETURNED (đã trả hàng về shop) -> REFUNDED (hoàn tiền) sau 2 ngày không có phản hồi từ store
     */
    @Scheduled(cron = "0 0 1 * * *") // Chạy lúc 01:00 mỗi ngày
    @Transactional
    public void autoConfirmReturnAndRefund() {
        log.info("=== [OrderScheduledService] Bắt đầu kiểm tra return request RETURNED quá 2 ngày ===");

        try {
            // Tìm các return request đã trả hàng về shop (RETURNED) quá 2 ngày
            LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
            List<ReturnRequest> returnedRequests = returnRequestRepository.findByStatusAndUpdatedAtBefore(
                    ReturnRequest.ReturnStatus.RETURNED.name(), twoDaysAgo);

            if (returnedRequests.isEmpty()) {
                log.info("[OrderScheduledService] Không có return request nào cần tự động xác nhận hoàn tiền");
                return;
            }

            log.info("[OrderScheduledService] Tìm thấy {} return request cần tự động hoàn tiền", returnedRequests.size());

            int successCount = 0;
            int failCount = 0;

            for (ReturnRequest returnRequest : returnedRequests) {
                try {
                    Order order = returnRequest.getOrder();
                    
                    // Cập nhật trạng thái sang REFUNDED
                    returnRequest.setStatus(ReturnRequest.ReturnStatus.REFUNDED.name());
                    returnRequest.setStoreResponse("Tự động xác nhận hoàn tiền do store không phản hồi trong 2 ngày");
                    returnRequestRepository.save(returnRequest);

                    // Tự động hoàn tiền qua MoMo
                    try {
                        refundService.createRefundRequest(order, returnRequest.getRefundAmount());
                        log.info("[OrderScheduledService] Auto refund initiated for return request {}, amount: {}",
                                returnRequest.getId(), returnRequest.getRefundAmount());
                    } catch (Exception e) {
                        log.error("[OrderScheduledService] Error processing auto refund: {}", e.getMessage());
                        
                        // Nếu hoàn tiền tự động thất bại, thông báo admin xử lý thủ công
                        try {
                            notificationService.createAdminNotification(
                                    "Lỗi hoàn tiền tự động",
                                    String.format(
                                            "Return request #%s - Đơn hàng #%s: Không thể hoàn tiền tự động %,.0f đ cho khách hàng %s. Lỗi: %s. Vui lòng xử lý thủ công.",
                                            returnRequest.getId(),
                                            order.getId(),
                                            returnRequest.getRefundAmount().doubleValue(),
                                            returnRequest.getBuyer().getFullName(),
                                            e.getMessage()),
                                    "REFUND_REQUEST",
                                    order.getId());
                        } catch (Exception notifEx) {
                            log.warn("[OrderScheduledService] Error sending admin notification: {}", notifEx.getMessage());
                        }
                        throw e;
                    }

                    // Thông báo cho khách hàng
                    try {
                        notificationService.createUserNotification(returnRequest.getBuyer().getId(),
                                "Hoàn tiền tự động",
                                String.format("Yêu cầu trả hàng #%s của bạn đã được tự động xác nhận và hoàn tiền %,.0f đ do cửa hàng không phản hồi trong 2 ngày.",
                                        returnRequest.getId(),
                                        returnRequest.getRefundAmount().doubleValue()),
                                order.getId());
                    } catch (Exception e) {
                        log.warn("[OrderScheduledService] Lỗi gửi thông báo cho buyer: {}", e.getMessage());
                    }

                    // Thông báo cho shop
                    try {
                        notificationService.createStoreNotification(returnRequest.getStore().getId(),
                                "Tự động hoàn tiền",
                                String.format("Yêu cầu trả hàng #%s đã được hệ thống tự động xác nhận và hoàn tiền do bạn không phản hồi trong 2 ngày.",
                                        returnRequest.getId()),
                                order.getId());
                    } catch (Exception e) {
                        log.warn("[OrderScheduledService] Lỗi gửi thông báo cho store: {}", e.getMessage());
                    }

                    successCount++;
                    log.info("[OrderScheduledService] Đã tự động hoàn tiền cho return request #{}", returnRequest.getId());

                } catch (Exception e) {
                    failCount++;
                    log.error("[OrderScheduledService] Lỗi khi xử lý return request #{}: {}", 
                            returnRequest.getId(), e.getMessage());
                }
            }

            log.info("=== [OrderScheduledService] Kết thúc auto refund: {} thành công, {} thất bại ===", successCount, failCount);

        } catch (Exception e) {
            log.error("[OrderScheduledService] Lỗi khi chạy auto refund scheduled task: {}", e.getMessage(), e);
        }
    }

    /**
     * Tự động hủy đơn hàng thanh toán online chưa thanh toán quá 1 giờ và hoàn trả tồn kho
     * Chạy mỗi 15 phút
     * 
     * Flow: PENDING + PaymentStatus.UNPAID + (VNPAY/MOMO) + createdAt > 1h -> CANCELLED + hoàn trả stock
     */
    @Scheduled(cron = "0 */15 * * * *") // Chạy mỗi 15 phút
    @Transactional
    public void autoCancelUnpaidOnlineOrders() {
        log.info("=== [OrderScheduledService] Bắt đầu kiểm tra đơn hàng online chưa thanh toán quá 1 giờ ===");

        try {
            // Tìm các đơn hàng PENDING + UNPAID + (VNPAY/MOMO) được tạo quá 1 giờ
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<String> onlinePaymentMethods = List.of("VNPAY", "MOMO");
            
            List<Order> onlineUnpaidOrders = orderRepository
                    .findByPaymentStatusAndStatusAndPaymentMethodInAndCreatedAtBefore(
                            Order.PaymentStatus.UNPAID.name(), 
                            Order.OrderStatus.PENDING.name(),
                            onlinePaymentMethods,
                            oneHourAgo);

            if (onlineUnpaidOrders.isEmpty()) {
                log.info("[OrderScheduledService] Không có đơn hàng online nào cần tự động hủy");
                return;
            }

            log.info("[OrderScheduledService] Tìm thấy {} đơn hàng online chưa thanh toán cần hủy", 
                    onlineUnpaidOrders.size());

            int successCount = 0;
            int failCount = 0;

            for (Order order : onlineUnpaidOrders) {
                try {
                    // Hoàn trả tồn kho trước khi hủy đơn
                    List<OrderItem> orderItems = 
                            orderItemRepository.findByOrderId(order.getId());
                    
                    for (OrderItem item : orderItems) {
                        ProductVariant productVariant = 
                                productVariantRepository.findById(item.getProductVariant().getId())
                                        .orElse(null);

                        if (productVariant == null) {
                            log.warn("[OrderScheduledService] Không tìm thấy product variant {}", 
                                    item.getProductVariant().getId());
                            continue;
                        }

                        // Hoàn trả stock đúng cách (tổng stock hoặc stock theo màu)
                        if (item.getColorId() != null && productVariant.getColors() != null
                                && !productVariant.getColors().isEmpty()) {
                            // Có màu sắc -> hoàn trả stock cho màu đó và cập nhật tổng stock
                            ProductVariant.ColorOption color = 
                                    productVariant.getColors().stream()
                                            .filter(c -> c.getId().equals(item.getColorId()))
                                            .findFirst()
                                            .orElse(null);

                            if (color != null) {
                                int oldStock = color.getStock();
                                color.setStock(color.getStock() + item.getQuantity());
                                log.info("[OrderScheduledService] Hoàn trả stock màu {}: {} -> {} (+{})",
                                        item.getColorId(), oldStock, color.getStock(), item.getQuantity());

                                // Cập nhật tổng stock = tổng stock của tất cả màu
                                int totalStock = productVariant.getColors().stream()
                                        .mapToInt(ProductVariant.ColorOption::getStock)
                                        .sum();
                                productVariant.setStock(totalStock);
                            }
                        } else {
                            // Không có màu sắc -> hoàn trả stock tổng
                            int oldStock = productVariant.getStock();
                            productVariant.setStock(productVariant.getStock() + item.getQuantity());
                            log.info("[OrderScheduledService] Hoàn trả stock: {} -> {} (+{})",
                                    oldStock, productVariant.getStock(), item.getQuantity());
                        }

                        productVariantRepository.save(productVariant);
                    }

                    // Cập nhật trạng thái đơn hàng sang CANCELLED
                    order.setStatus(Order.OrderStatus.CANCELLED.name());
                    order.setRejectReason("Tự động hủy do không thanh toán trong vòng 1 giờ");
                    orderRepository.save(order);

                    // Thông báo cho khách hàng
                    try {
                        notificationService.createUserNotification(order.getBuyer().getId(),
                                "Đơn hàng đã bị hủy",
                                String.format("Đơn hàng #%s đã bị hủy tự động do bạn chưa thanh toán trong vòng 1 giờ. " +
                                        "Vui lòng đặt hàng lại nếu bạn vẫn muốn mua.", 
                                        order.getId()),
                                order.getId());
                    } catch (Exception e) {
                        log.warn("[OrderScheduledService] Lỗi gửi thông báo cho buyer: {}", e.getMessage());
                    }

                    // Thông báo cho shop
                    try {
                        notificationService.createStoreNotification(order.getStore().getId(),
                                "Đơn hàng đã bị hủy",
                                String.format("Đơn hàng #%s đã bị hệ thống tự động hủy do khách hàng chưa thanh toán trong vòng 1 giờ.",
                                        order.getId()),
                                order.getId());
                    } catch (Exception e) {
                        log.warn("[OrderScheduledService] Lỗi gửi thông báo cho store: {}", e.getMessage());
                    }

                    successCount++;
                    log.info("[OrderScheduledService] Đã hủy đơn hàng #{} và hoàn trả stock", order.getId());

                } catch (Exception e) {
                    failCount++;
                    log.error("[OrderScheduledService] Lỗi khi hủy đơn hàng #{}: {}", 
                            order.getId(), e.getMessage());
                }
            }

            log.info("=== [OrderScheduledService] Kết thúc auto cancel: {} thành công, {} thất bại ===", 
                    successCount, failCount);

        } catch (Exception e) {
            log.error("[OrderScheduledService] Lỗi khi chạy auto cancel scheduled task: {}", e.getMessage(), e);
        }
    }
}
