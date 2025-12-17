package com.example.e_commerce_techshop.services.order;


import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ShipmentResponse;
import com.example.e_commerce_techshop.responses.buyer.OrderResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IOrderService {

    List<Order> checkout(User user, OrderDTO orderDTO) throws Exception;

    // Lấy lịch sử đơn hàng
    Page<Order> getOrderHistory(User user, String status, Pageable pageable) throws Exception;

    // Lấy chi tiết đơn hàng
    Order getOrderDetail(User user, String orderId) throws Exception;

    Order getOrderById(String orderId) throws Exception;

    // Hủy đơn hàng
    void cancelOrder(User user, String orderId) throws Exception;
    
    // Xác nhận hoàn tất đơn hàng (buyer)
    Order completeOrder(User user, String orderId) throws Exception;
    
    // ===== SELLER METHODS =====

    Page<OrderResponse> getStoreOrders(String storeId, String status, Pageable pageable) throws Exception;
    
    Order getStoreOrderDetail(String storeId, String orderId) throws Exception;
    
    Order confirmOrder(String storeId, String orderId) throws Exception;

    Order rejectOrder(String storeId, String orderId, String reason) throws Exception;

    Map<String, Long> countOrdersByStatus(String storeId) throws Exception;
    
    // ===== PAYMENT METHODS =====
    
    /**
     * Cập nhật trạng thái thanh toán cho đơn hàng
     * @param orderId Mã đơn hàng
     * @param paymentStatus Trạng thái thanh toán (PAID, FAILED, UNPAID)
     * @param momoTransId Mã giao dịch MoMo (nếu có)
     * @throws Exception
     */
    void updatePaymentStatus(String orderId, String paymentStatus, Long momoTransId) throws Exception;

    /**
     * Lấy thông tin hoàn tiền của đơn hàng (nếu có)
     * @param user Buyer
     * @param orderId Mã đơn hàng
     * @return RefundInfo hoặc null nếu chưa có hoàn tiền
     * @throws Exception
     */
    OrderResponse.RefundInfo getOrderRefundInfo(User user, String orderId) throws Exception;

    /**
     * Lấy thông tin vận chuyển trả hàng của đơn hàng (nếu có)
     * @param user Buyer
     * @param orderId Mã đơn hàng
     * @return ShipmentResponse hoặc null nếu chưa có shipment trả hàng
     * @throws Exception
     */
    ShipmentResponse getReturnShipmentInfo(User user, String orderId) throws Exception;
}