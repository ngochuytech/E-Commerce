package com.example.e_commerce_techshop.services.order;


import com.example.e_commerce_techshop.dtos.OrderDTO;
import com.example.e_commerce_techshop.models.Order;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IOrderService {
    
    /**
     * Tạo đơn hàng từ cart
     * @param userEmail Email của user
     * @param orderDTO Thông tin checkout
     * @return OrderDTO
     */
    List<Order> checkout(String userEmail, OrderDTO orderDTO) throws Exception;
    
    /**
     * Lấy lịch sử đơn hàng
     * @param userEmail Email của user
     * @param page Số trang (0-based)
     * @param size Kích thước trang
     * @param status Trạng thái đơn hàng
     * @return Page<OrderResponse>
     */
    Page<Order> getOrderHistory(String userEmail, int page, int size, String status) throws Exception;
    
    /**
     * Lấy chi tiết đơn hàng
     * @param userEmail Email của user
     * @param orderId ID đơn hàng
     * @return OrderResponse
     */
    Order getOrderDetail(String userEmail, String orderId) throws Exception;
    
    /**
     * Hủy đơn hàng
     * @param userEmail Email của user
     * @param orderId ID đơn hàng
     * @return OrderResponse
     */
    Order cancelOrder(String userEmail, String orderId) throws Exception;
    
    /**
     * Đếm số đơn hàng theo trạng thái
     * @param userEmail Email của user
     * @return Map<String, Long>
     */
    Map<String, Long> getOrderCount(String userEmail) throws Exception;
}