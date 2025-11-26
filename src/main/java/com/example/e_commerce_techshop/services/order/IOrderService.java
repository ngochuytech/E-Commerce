package com.example.e_commerce_techshop.services.order;


import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {

    List<Order> checkout(User user, OrderDTO orderDTO) throws Exception;

    // Lấy lịch sử đơn hàng
    Page<Order> getOrderHistory(User user, String status, Pageable pageable) throws Exception;

    // Lấy chi tiết đơn hàng
    Order getOrderDetail(User user, String orderId) throws Exception;

    // Hủy đơn hàng
    void cancelOrder(User user, String orderId) throws Exception;
    
    // ===== SELLER METHODS =====

    //Lấy đơn hàng của store (cho seller)
    Page<Order> getStoreOrders(String storeId, int page, int size, String status) throws Exception;
    
    //Lấy chi tiết đơn hàng của store
    Order getStoreOrderDetail(String storeId, String orderId) throws Exception;
    
    // Xác nhận đơn hàng (cho seller)
    Order confirmOrder(String storeId, String orderId) throws Exception;

    Order rejectOrder(String storeId, String orderId, String reason) throws Exception;
}