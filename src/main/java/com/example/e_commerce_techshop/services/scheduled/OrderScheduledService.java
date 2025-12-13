package com.example.e_commerce_techshop.services.scheduled;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.services.notification.INotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderScheduledService {

    private final OrderRepository orderRepository;
    private final INotificationService notificationService;

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
}
