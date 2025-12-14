package com.example.e_commerce_techshop.services.shipment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.AdminRevenue;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.AdminRevenueRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ReturnRequestRepository;
import com.example.e_commerce_techshop.repositories.ShipmentRepository;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentService implements IShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final AdminRevenueRepository adminRevenueRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final INotificationService notificationService;
    private final IWalletService walletService;

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
        history.add(LocalDateTime.now() + ": Tạo đơn vận chuyển (READY_TO_PICK)");

        Shipment.Address fromAddress = Shipment.Address.builder()
                .homeAddress(order.getStore().getAddress().getHomeAddress())
                .ward(order.getStore().getAddress().getWard())
                .province(order.getStore().getAddress().getProvince())
                .suggestedName(order.getStore().getAddress().getSuggestedName())
                .build();

        Shipment.Address toAddress = Shipment.Address.builder()
                .homeAddress(order.getAddress().getHomeAddress())
                .ward(order.getAddress().getWard())
                .province(order.getAddress().getProvince())
                .suggestedName(order.getAddress().getSuggestedName())
                .build();

        Shipment shipment = Shipment.builder()
                .order(order)
                .store(order.getStore())
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .shippingFee(order.getShippingFee())
                .status(Shipment.ShipmentStatus.READY_TO_PICK.name())
                .history(history)
                .expectedDeliveryDate(LocalDateTime.now().plusDays(2)) // Dự kiến 2 ngày
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Thông báo cho khách hàng
        try {
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Đơn hàng đã sẵn sàng để giao",
                    String.format("Đơn hàng #%s đã được chuẩn bị xong và sẵn sàng để giao cho shipper.", order.getId()),
                    order.getId());
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
                    shipments.size());
        } else {
            List<Shipment> shipments = shipmentRepository.findByStoreId(storeId);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), shipments.size());
            return new org.springframework.data.domain.PageImpl<>(
                    shipments.subList(start, end),
                    pageable,
                    shipments.size());
        }
    }

    /**
     * Lấy danh sách shipment theo trạng thái
     */
    @Override
    public Page<Shipment> getShipmentsByStatus(String status, Pageable pageable) throws Exception {
        return shipmentRepository.findByStatus(status, pageable);
    }

    @Override
    public Map<String, Long> getShipmentCountByStatus(String storeId) throws Exception {
        Map<String, Long> statusCounts = new HashMap<>();

        long ready_to_pick = shipmentRepository.countByStatus(
                Shipment.ShipmentStatus.READY_TO_PICK.name());
        long delivered = shipmentRepository.countByStatus(
                Shipment.ShipmentStatus.DELIVERED.name());
        long returned = shipmentRepository.countByStatus(
                Shipment.ShipmentStatus.RETURNED.name());

        statusCounts.put("readyToPick", ready_to_pick);
        statusCounts.put("delivered", delivered);
        statusCounts.put("returned", returned);
        return statusCounts;
    }

    @Override
    public Page<Shipment> getShipperShipments(User shipper, Pageable pageable) throws Exception {
        return shipmentRepository.findByCarrier(shipper.getId(), pageable);
    }

    /**
     * Shipper xác nhận đã lấy hàng (PICKING -> PICKED)
     */
    @Override
    @Transactional
    public Shipment pickedShipment(String shipmentId, User shipper) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        if (!Shipment.ShipmentStatus.PICKING.name().equals(shipment.getStatus())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển sang PICKED. Trạng thái hiện tại: %s", shipment.getStatus()));
        }

        shipment.setStatus(Shipment.ShipmentStatus.PICKED.name());
        shipment.setCarrier(shipper);

        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(
                LocalDateTime.now() + ": Shipper " + shipper.getFullName() + " đã lấy hàng, bắt đầu giao (SHIPPING)");

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Thông báo
        try {
            Order order = shipment.getOrder();
            if (shipment.isReturnShipment()) {
                // Thông báo cho người mua
                notificationService.createUserNotification(order.getBuyer().getId(),
                        "Shipper đã lấy hàng trả về shop",
                        String.format("Shipper %s đã lấy đơn hàng #%s để trả về shop.",
                                shipper.getFullName(), order.getId()),
                        order.getId());
            } else {
                // Thông báo cho người bán (store)
                notificationService.createStoreNotification(order.getStore().getId(),
                        "Shipper đã lấy hàng",
                        String.format("Shipper %s đã lấy đơn hàng #%s từ shop.",
                                shipper.getFullName(), order.getId()),
                        order.getId());
            }
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Shipper xác nhận đã giao hàng thành công (SHIPPING -> DELIVERED)
     */
    @Override
    @Transactional
    public Shipment deliverShipment(String shipmentId, User shipper) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        // Kiểm tra trạng thái hiện tại
        if (!Shipment.ShipmentStatus.SHIPPING.name().equals(shipment.getStatus())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển sang DELIVERED. Trạng thái hiện tại: %s", shipment.getStatus()));
        }

        shipment.setStatus(Shipment.ShipmentStatus.DELIVERED.name());
        shipment.setCarrier(shipper);

        // Cập nhật lịch sử
        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(LocalDateTime.now() + ": Đã giao hàng thành công (DELIVERED)");

        Shipment savedShipment = shipmentRepository.save(shipment);

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

        // Thông báo cho khách hàng và shop
        try {
            // Thông báo cho khách hàng
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Đơn hàng đã được giao thành công",
                    String.format("Đơn hàng #%s đã được giao đến bạn. Bạn cần kiểm tra và xác nhận!", order.getId()),
                    order.getId());

            // Thông báo cho người bán (store)
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Đơn hàng đã được giao",
                    String.format("Đơn hàng #%s đã được giao thành công", order.getId()),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Shipper báo giao hàng thất bại (SHIPPING -> DELIVERED_FAIL)
     */
    @Override
    @Transactional
    public Shipment deliverFailShipment(String shipmentId, String reason) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        // Kiểm tra trạng thái hiện tại
        if (!Shipment.ShipmentStatus.SHIPPING.name().equals(shipment.getStatus())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển sang DELIVERED_FAIL. Trạng thái hiện tại: %s",
                            shipment.getStatus()));
        }

        shipment.setStatus(Shipment.ShipmentStatus.DELIVERED_FAIL.name());

        // Cập nhật lịch sử
        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(LocalDateTime.now() + ": Giao hàng thất bại (DELIVERED_FAIL)");
        shipment.getHistory().add(LocalDateTime.now() + ": Lý do thất bại - " + reason);

        Shipment savedShipment = shipmentRepository.save(shipment);

        Order order = shipment.getOrder();

        // Thông báo cho khách hàng và shop
        try {
            // Thông báo cho khách hàng
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Giao hàng thất bại",
                    String.format("Đơn hàng %s giao hàng thất bại.",
                            order.getId(), reason),
                    order.getId());

            // Thông báo cho người bán (store)
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Giao hàng thất bại",
                    String.format("Đơn hàng #%s giao hàng thất bại. Lý do: %s. Chúng tôi sẽ liên hệ lại để giao lại.",
                            order.getId(), reason),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Shipper xác nhận đang trên đường đến lấy hàng (READY_TO_PICK -> PICKING)
     */
    @Override
    @Transactional
    public Shipment pickingShipment(String shipmentId, User shipper) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        // Kiểm tra trạng thái hiện tại phải là READY_TO_PICK
        if (!Shipment.ShipmentStatus.READY_TO_PICK.name().equals(shipment.getStatus())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển sang PICKING. Trạng thái hiện tại: %s", shipment.getStatus()));
        }

        shipment.setStatus(Shipment.ShipmentStatus.PICKING.name());
        shipment.setCarrier(shipper);

        // Cập nhật lịch sử
        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(
                LocalDateTime.now() + ": Shipper " + shipper.getFullName() + " đang trên đường đến lấy hàng (PICKING)");

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Thông báo
        try {
            Order order = shipment.getOrder();
            if (shipment.isReturnShipment()) {
                // Thông báo cho người mua
                notificationService.createUserNotification(order.getBuyer().getId(),
                        "Shipper đang đến lấy hàng trả về shop",
                        String.format(
                                "Shipper %s đang trên đường đến lấy đơn hàng #%s để trả về shop. Vui lòng chuẩn bị hàng sẵn sàng!",
                                shipper.getFullName(), order.getId()),
                        order.getId());
            } else {
                // Thông báo cho người bán (store)
                notificationService.createStoreNotification(order.getStore().getId(),
                        "Shipper đang đến lấy hàng",
                        String.format(
                                "Shipper %s đang trên đường đến lấy đơn hàng #%s. Vui lòng chuẩn bị hàng sẵn sàng!",
                                shipper.getFullName(), order.getId()),
                        order.getId());
            }
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Shipper xác nhận bắt đầu giao hàng cho khách (PICKED -> SHIPPING)
     */
    @Override
    @Transactional
    public Shipment shippingShipment(String shipmentId, User shipper) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        // Kiểm tra trạng thái hiện tại phải là PICKED
        if (!Shipment.ShipmentStatus.PICKED.name().equals(shipment.getStatus())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển sang SHIPPING. Trạng thái hiện tại: %s", shipment.getStatus()));
        }

        shipment.setStatus(Shipment.ShipmentStatus.SHIPPING.name());
        shipment.setCarrier(shipper);

        // Cập nhật lịch sử
        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(LocalDateTime.now() + ": Shipper " + shipper.getFullName()
                + " đang trên đường giao hàng cho khách (SHIPPING)");

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Cập nhật trạng thái order sang SHIPPING
        Order order = shipment.getOrder();
        order.setStatus(Order.OrderStatus.SHIPPING.name());
        orderRepository.save(order);

        // Thông báo cho khách hàng
        try {
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Đơn hàng đang được giao",
                    String.format("Đơn hàng #%s đang trên đường giao đến bạn. Vui lòng chú ý điện thoại!",
                            order.getId()),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Shipper báo đang trả hàng về cho shop sau khi giao thất bại (DELIVERED_FAIL
     * -> RETURNING)
     */
    @Override
    @Transactional
    public Shipment returningShipment(String shipmentId, User shipper) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        // Kiểm tra trạng thái hiện tại phải là DELIVERED_FAIL
        if (!Shipment.ShipmentStatus.DELIVERED_FAIL.name().equals(shipment.getStatus())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển sang RETURNING. Trạng thái hiện tại: %s", shipment.getStatus()));
        }

        shipment.setStatus(Shipment.ShipmentStatus.RETURNING.name());
        shipment.setCarrier(shipper);

        // Cập nhật lịch sử
        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(LocalDateTime.now() + ": Shipper đang trả hàng về cho shop (RETURNING)");

        Shipment savedShipment = shipmentRepository.save(shipment);

        Order order = shipment.getOrder();

        // Thông báo cho shop
        try {
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Đơn hàng đang được trả về",
                    String.format(
                            "Đơn hàng #%s giao thất bại và đang được shipper trả về cửa hàng. Vui lòng chuẩn bị nhận hàng!",
                            order.getId()),
                    order.getId());

            // Thông báo cho khách hàng
            if (!shipment.isReturnShipment()) {
                notificationService.createUserNotification(order.getBuyer().getId(),
                        "Đơn hàng không giao được",
                        String.format(
                                "Đơn hàng #%s không giao được và đang được trả về cửa hàng. Vui lòng liên hệ shop nếu cần hỗ trợ.",
                                order.getId()),
                        order.getId());
            }
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }

    /**
     * Shipper xác nhận đã trả hàng về cho shop (RETURNING -> RETURNED)
     */
    @Override
    @Transactional
    public Shipment returnedShipment(String shipmentId, User shipper) throws Exception {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy shipment với ID: " + shipmentId));

        // Kiểm tra trạng thái hiện tại phải là RETURNING
        if (!Shipment.ShipmentStatus.RETURNING.name().equals(shipment.getStatus())) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển sang RETURNED. Trạng thái hiện tại: %s", shipment.getStatus()));
        }

        shipment.setStatus(Shipment.ShipmentStatus.RETURNED.name());
        shipment.setCarrier(shipper);

        // Cập nhật lịch sử
        if (shipment.getHistory() == null) {
            shipment.setHistory(new ArrayList<>());
        }
        shipment.getHistory().add(LocalDateTime.now() + ": Đã trả hàng về cho shop thành công (RETURNED)");

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Cập nhật trạng thái order sang RETURNED
        Order order = shipment.getOrder();
        if (shipment.isReturnShipment()) {
            order.setStatus(Order.OrderStatus.RETURNED.name());

            // Cập nhật trạng thái ReturnRequest sang RETURNED
            returnRequestRepository.findByOrderId(order.getId()).ifPresent(returnRequest -> {
                returnRequest.setStatus(ReturnRequest.ReturnStatus.RETURNED.name());
                returnRequestRepository.save(returnRequest);
                System.out.println("Updated ReturnRequest status to RETURNED for order: " + order.getId());
            });
        } else {
            order.setStatus(Order.OrderStatus.CANCELLED.name());
        }
        orderRepository.save(order);

        // Thông báo cho shop
        try {
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Đơn hàng đã được trả về",
                    String.format("Đơn hàng #%s đã được shipper trả về thành công. Vui lòng kiểm tra và nhập lại kho!",
                            order.getId()),
                    order.getId());

            // Thông báo cho khách hàng
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Đơn hàng đã hoàn trả về shop",
                    String.format("Đơn hàng #%s đã được trả về cửa hàng %s.",
                            order.getId(), order.getStore().getName()),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
        }

        return savedShipment;
    }
}
