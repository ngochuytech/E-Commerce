package com.example.e_commerce_techshop.services.shipment;

import com.example.e_commerce_techshop.models.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IShipmentService {

    /**
     * Tạo shipment khi người bán xác nhận đơn hàng
     */
    Shipment createShipment(String orderId) throws Exception;

    /**
     * Lấy thông tin shipment theo order ID
     */
    Shipment getShipmentByOrderId(String orderId) throws Exception;

    /**
     * Lấy danh sách shipment của store
     */
    Page<Shipment> getStoreShipments(String storeId, String status, Pageable pageable) throws Exception;

    Map<String, Long> getShipmentCountByStatus(String storeId) throws Exception;

    /**
     * Cập nhật trạng thái shipment (dành cho vận chuyển cập nhật)
     */
    Shipment updateShipmentStatus(String shipmentId, String newStatus) throws Exception;

    /**
     * Lấy danh sách shipment theo trạng thái
     */
    List<Shipment> getShipmentsByStatus(String status) throws Exception;
}
