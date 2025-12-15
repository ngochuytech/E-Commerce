package com.example.e_commerce_techshop.services.shipment;

import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IShipmentService {
    /**
     * Tạo shipment khi người bán xác nhận đơn hàng
     */
    Shipment createShipment(String orderId) throws Exception;

    Shipment getShipmentById(String shipmentId) throws Exception;

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
    Shipment pickingShipment(String shipmentId, User shipper) throws Exception;

    Shipment pickedShipment(String shipmentId, User shipper) throws Exception;

    Shipment shippingShipment(String shipmentId, User shipper) throws Exception;

    Shipment deliverShipment(String shipmentId, User shipper) throws Exception;

    Shipment deliverFailShipment(String shipmentId, String reason) throws Exception;

    Shipment returningShipment(String shipmentId, User shipper) throws Exception;

    Shipment returnedShipment(String shipmentId, User shipper) throws Exception;

    /**
     * Lấy danh sách shipment theo trạng thái
     */
    Page<Shipment> getShipmentsByStatus(String status, Pageable pageable) throws Exception;

    Page<Shipment> getShipperShipments(User shipper, Pageable pageable) throws Exception;
}
