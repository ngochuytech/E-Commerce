package com.example.e_commerce_techshop.services.shipment;

import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ShipmentRepository;
import com.example.e_commerce_techshop.services.notification.INotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService implements IShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final INotificationService notificationService;

    /**
     * Tạo shipment khi người bán xác nhận đơn hàng
     */
    @Override
    @Transactional
    public Shipment createShipment(String orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra shipment đã tồn tại chưa
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            throw new IllegalStateException("Shipment cho đơn hàng này đã tồn tại");
        }

        // Tạo shipment mới
        List<String> history = new ArrayList<>();
        history.add(LocalDateTime.now() + ": Tạo đơn vận chuyển (PICKING_UP)");

        Shipment shipment = Shipment.builder()
                .order(order)
                .store(order.getStore())
                .address(order.getAddress())
                .shippingFee(order.getShippingFee())
                .status(Shipment.ShipmentStatus.PICKING_UP.name())
                .history(history)
                .expectedDeliveryDate(LocalDateTime.now().plusDays(2)) // Dự kiến 2 ngày
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Thông báo cho khách hàng
        try {
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Đơn hàng đang được chuẩn bị",
                    String.format("Cửa hàng %s đang chuẩn bị hàng cho đơn #%s. Dự kiến giao: %s",
                            order.getStore().getName(), orderId, "2 ngày"),
                        orderId);
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Lấy thông tin shipment theo order ID
     */
    @Override
    public Shipment getShipmentByOrderId(String orderId) throws Exception {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment cho đơn hàng: " + orderId));
    }

    /**
     * Lấy danh sách shipment của store
     */
    @Override
    public Page<Shipment> getStoreShipments(String storeId, String status, Pageable pageable) throws Exception {
        if (status != null && !status.isEmpty()) {
            List<Shipment> shipments = shipmentRepository.findByStoreIdAndStatus(storeId, status);
            // Convert to Page (simplified)
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), shipments.size());
            return new org.springframework.data.domain.PageImpl<>(
                    shipments.subList(start, end),
                    pageable,
                    shipments.size()
            );
        } else {
            List<Shipment> shipments = shipmentRepository.findByStoreId(storeId);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), shipments.size());
            return new org.springframework.data.domain.PageImpl<>(
                    shipments.subList(start, end),
                    pageable,
                    shipments.size()
            );
        }
    }

    /**
     * Cập nhật trạng thái shipment (dành cho vận chuyển cập nhật)
     */
    @Override
    @Transactional
    public Shipment updateShipmentStatus(String shipmentId, String newStatus) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        // Kiểm tra trạng thái hợp lệ
        if (!isValidStatusTransition(shipment.getStatus(), newStatus)) {
            throw new IllegalArgumentException(
                    String.format("Không thể chuyển từ %s sang %s", shipment.getStatus(), newStatus));
        }

        shipment.setStatus(newStatus);
        
        // Cập nhật lịch sử
        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(LocalDateTime.now() + ": Cập nhật trạng thái " + newStatus);

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Thông báo cho khách hàng
        try {
            Order order = shipment.getOrder();
            String message = getStatusMessage(newStatus);
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Cập nhật vận chuyển",
                    String.format("Đơn hàng #%s: %s", order.getId(), message),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Lấy danh sách shipment theo trạng thái
     */
    @Override
    public List<Shipment> getShipmentsByStatus(String status) throws Exception {
        return shipmentRepository.findByStatus(status);
    }

    /**
     * Kiểm tra chuyển trạng thái hợp lệ
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "PICKING_UP":
                return "SHIPPING".equals(newStatus);
            case "SHIPPING":
                return "DELIVERED".equals(newStatus) || "FAILED".equals(newStatus);
            case "DELIVERED":
                return false;
            default:
                return false;
        }
    }

    /**
     * Lấy thông báo theo trạng thái
     */
    private String getStatusMessage(String status) {
        return switch (status) {
            case "PICKING_UP" -> "Nhân viên đang lấy hàng";
            case "SHIPPING" -> "Hàng đang được vận chuyển";
            case "DELIVERED" -> "Hàng đã được giao thành công";
            case "FAILED" -> "Giao hàng thất bại, sẽ thử lại";
            default -> "Cập nhật: " + status;
        };
    }
}
