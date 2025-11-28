package com.example.e_commerce_techshop.services.scheduled;

import com.example.e_commerce_techshop.models.AdminRevenue;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.repositories.AdminRevenueRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ShipmentRepository;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentScheduledService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final AdminRevenueRepository adminRevenueRepository;
    private final INotificationService notificationService;
    private final IWalletService walletService;

    /**
     * Tự động chuyển trạng thái từ PICKING_UP -> SHIPPING sau 3 phút
     * Chạy mỗi 5 phút kiểm tra 1 lần
     */
    @Scheduled(fixedDelay = 180000) // 3 phút = 180000 ms
    @Transactional
    public void autoPickingUpToShipping() {
        try {
            LocalDateTime threeMinutesAgo = LocalDateTime.now().minusMinutes(3);

            // Lấy tất cả shipment ở trạng thái PICKING_UP và được tạo > 30 phút trước
            List<Shipment> shipments = shipmentRepository.findByStatusAndCreatedAtBefore(
                    Shipment.ShipmentStatus.PICKING_UP.name(),
                    threeMinutesAgo);

            for (Shipment shipment : shipments) {
                shipment.setStatus(Shipment.ShipmentStatus.SHIPPING.name());

                // Cập nhật lịch sử
                if (shipment.getHistory() == null) {
                    shipment.setHistory(new ArrayList<>());
                }
                shipment.getHistory().add(LocalDateTime.now() + ": Chuyển sang SHIPPING");

                shipmentRepository.save(shipment);

                // Thông báo cho khách hàng
                try {
                    Order order = shipment.getOrder();
                    notificationService.createUserNotification(order.getBuyer().getId(),
                            "Đơn hàng của bạn đã được nhân viên lấy hàng",
                            String.format("Đơn hàng #%s đang được vận chuyển đến bạn", order.getId()),
                        order.getId());
                } catch (Exception e) {
                    System.err.println("Error sending notification: " + e.getMessage());
                }
            }

            if (!shipments.isEmpty()) {
                System.out
                        .println("[ShipmentScheduled] Cập nhật " + shipments.size() + " đơn từ PICKING_UP -> SHIPPING");
            }

        } catch (Exception e) {
            System.err.println("[ShipmentScheduled] Error in autoPickingUpToShipping: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tự động chuyển trạng thái từ SHIPPING -> DELIVERED sau 4 phút
     * Chạy mỗi 5 phút kiểm tra 1 lần
     */
    @Scheduled(fixedDelay = 300000) // 5 phút = 300000 ms
    @Transactional
    public void autoShippingToDelivered() {
        try {
            LocalDateTime fourMinutesAgo = LocalDateTime.now().minusMinutes(4);

            // Lấy tất cả shipment ở trạng thái SHIPPING và được tạo > 4 phút trước
            List<Shipment> shipments = shipmentRepository.findByStatusAndCreatedAtBefore(
                    Shipment.ShipmentStatus.SHIPPING.name(),
                    fourMinutesAgo);

            for (Shipment shipment : shipments) {
                shipment.setStatus(Shipment.ShipmentStatus.DELIVERED.name());

                // Cập nhật lịch sử
                if (shipment.getHistory() == null) {
                    shipment.setHistory(new ArrayList<>());
                }
                shipment.getHistory().add(LocalDateTime.now() + ": Đã giao hàng thành công");

                shipmentRepository.save(shipment);

                // Cập nhật trạng thái order sang DELIVERED
                Order order = shipment.getOrder();
                order.setStatus(Order.OrderStatus.DELIVERED.name());
                orderRepository.save(order);

                // Tạo AdminRevenue để lưu phí dịch vụ khi đơn được giao
                try {
                    AdminRevenue adminRevenue = AdminRevenue.builder()
                            .order(order)
                            .amount(order.getServiceFee())
                            .revenueType("SERVICE_FEE")
                            .description(
                                    String.format("Phí dịch vụ từ đơn hàng #%s - Trạng thái DELIVERED", order.getId()))
                            .build();
                    adminRevenueRepository.save(adminRevenue);

                    // Tạo AdminRevenue cho discount loss từ platform (sàn chịu)
                    if (order.getPlatformDiscountAmount() != null
                            && order.getPlatformDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        AdminRevenue platformDiscountLoss = AdminRevenue.builder()
                                .order(order)
                                .amount(order.getPlatformDiscountAmount())
                                .revenueType("PLATFORM_DISCOUNT_LOSS")
                                .description(String.format("Tiền giảm giá sàn chịu từ đơn hàng #%s", order.getId()))
                                .build();
                        adminRevenueRepository.save(platformDiscountLoss);
                    }
                } catch (Exception e) {
                    System.err.println("Error creating AdminRevenue: " + e.getMessage());
                }

                // Cộng tiền vào ví shop khi đơn hàng được giao thành công
                try {
                    // Số tiền shop nhận = Giá sản phẩm - Discount của shop - Phí dịch vụ platform
                    BigDecimal shopAmount = order.getProductPrice()
                        .subtract(order.getStoreDiscountAmount())
                        .subtract(order.getServiceFee());
                    
                    walletService.addOrderPaymentToWallet(
                        order.getStore().getId(),
                        order.getId(),
                        shopAmount,
                        String.format("Thanh toán từ đơn hàng #%s - Giá: %s, Discount shop: %s, Phí sàn: %s", 
                            order.getId(), 
                            order.getProductPrice(), 
                            order.getStoreDiscountAmount(),
                            order.getServiceFee())
                    );
                } catch (Exception e) {
                    System.err.println("Error adding payment to wallet: " + e.getMessage());
                }

                try {
                    // Thông báo cho khách hàng
                    notificationService.createUserNotification(order.getBuyer().getId(),
                            "Đơn hàng đã được giao thành công",
                            String.format("Đơn hàng #%s đã được giao đến bạn. Cảm ơn bạn đã mua sắm!", order.getId()),
                            order.getId());

                    // Thông báo cho người bán (store)
                    notificationService.createStoreNotification(order.getStore().getId(),
                            "Đơn hàng đã được giao",
                            String.format("Đơn hàng #%s đã được giao thành công", order.getId()),
                            order.getId());
                } catch (Exception e) {
                    System.err.println("Error sending notification: " + e.getMessage());
                }
            }

            if (!shipments.isEmpty()) {
                System.out
                        .println("[ShipmentScheduled] Cập nhật " + shipments.size() + " đơn từ SHIPPING -> DELIVERED");
            }

        } catch (Exception e) {
            System.err.println("[ShipmentScheduled] Error in autoShippingToDelivered: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra và cập nhật expectedDeliveryDate cho các shipment SHIPPING
     * Chạy mỗi 5 phút
     */
    @Scheduled(fixedDelay = 300000) // 5 phút = 300000 ms
    @Transactional
    public void updateExpectedDeliveryDate() {
        try {
            List<Shipment> shippingShipments = shipmentRepository.findByStatus(Shipment.ShipmentStatus.SHIPPING.name());

            for (Shipment shipment : shippingShipments) {
                if (shipment.getExpectedDeliveryDate() == null) {
                    // Nếu chưa có, set là 2 ngày từ bây giờ
                    shipment.setExpectedDeliveryDate(LocalDateTime.now().plusDays(2));
                    shipmentRepository.save(shipment);
                }
            }

            if (!shippingShipments.isEmpty()) {
                System.out.println(
                        "[ShipmentScheduled] Cập nhật expectedDeliveryDate cho " + shippingShipments.size() + " đơn");
            }

        } catch (Exception e) {
            System.err.println("[ShipmentScheduled] Error in updateExpectedDeliveryDate: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
