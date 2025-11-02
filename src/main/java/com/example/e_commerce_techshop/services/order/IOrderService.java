package com.example.e_commerce_techshop.services.order;


import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface IOrderService {
    
    /**
     * Tạo đơn hàng từ cart
     */
    List<Order> checkout(User user, OrderDTO orderDTO) throws Exception;
    
    /**
     * Lấy lịch sử đơn hàng
     */
    Page<Order> getOrderHistory(User user, String status, Pageable pageable) throws Exception;

    /**
     * Lấy chi tiết đơn hàng
     * @param userEmail Email của user
     * @param orderId ID đơn hàng
     * @return OrderResponse
     */
    Order getOrderDetail(User user, String orderId) throws Exception;
    
    /**
     * Hủy đơn hàng
     * @param userEmail Email của user
     * @param orderId ID đơn hàng
     */
    void cancelOrder(String userEmail, String orderId) throws Exception;
    
    /**
     * Đếm số đơn hàng theo trạng thái
     * @param userEmail Email của user
     * @return Map<String, Long>
     */
    Map<String, Long> getOrderCount(String userEmail) throws Exception;
    
    // ===== SELLER METHODS =====
    
    /**
     * Lấy đơn hàng của store (cho seller)
     * @param storeId ID của store
     * @param page Số trang
     * @param size Kích thước trang
     * @param status Trạng thái đơn hàng
     * @return Page<Order>
     */
    Page<Order> getStoreOrders(String storeId, int page, int size, String status) throws Exception;
    
    /**
     * Lấy chi tiết đơn hàng của store
     * @param storeId ID của store
     * @param orderId ID đơn hàng
     * @return Order
     */
    Order getStoreOrderDetail(String storeId, String orderId) throws Exception;
    
    /**
     * Cập nhật trạng thái đơn hàng (cho seller)
     * @param storeId ID của store
     * @param orderId ID đơn hàng
     * @param newStatus Trạng thái mới
     */
    Order updateOrderStatus(String storeId, String orderId, String newStatus) throws Exception;
    
    /**
     * Thống kê đơn hàng của store
     * @param storeId ID của store
     * @return Map<String, Object>
     */
    Map<String, Object> getStoreOrderStatistics(String storeId) throws Exception;
    
    /**
     * Lấy doanh thu của store theo khoảng thời gian
     * @param storeId ID của store
     * @param startDate Ngày bắt đầu (yyyy-MM-dd)
     * @param endDate Ngày kết thúc (yyyy-MM-dd)
     * @return Map<String, Object>
     */
    Map<String, Object> getStoreRevenue(String storeId, String startDate, String endDate) throws Exception;
}