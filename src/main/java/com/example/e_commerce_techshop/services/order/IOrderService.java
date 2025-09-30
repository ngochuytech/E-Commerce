package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.b2c.order.OrderDTO;
import com.example.e_commerce_techshop.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    
    // Create order (for buyers)
    void createOrder(OrderDTO orderDTO) throws Exception;
    
    // Update order status (for sellers)
    OrderResponse updateOrderStatus(String orderId, String status);
    
    // Get order by ID
    OrderResponse getOrderById(String orderId);
    
    // Get orders by store
    List<OrderResponse> getOrdersByStore(String storeId);
    
    // Get orders by store and status
    List<OrderResponse> getOrdersByStoreAndStatus(String storeId, String status);
    
    // Get recent orders by store
    List<OrderResponse> getRecentOrdersByStore(String storeId, int limit);
    
    // Cancel order
    OrderResponse cancelOrder(String orderId, String reason);
    
    // Get order statistics
    Long getOrderCountByStoreAndStatus(String storeId, String status);
    
    // Get orders by date range
    List<OrderResponse> getOrdersByDateRange(String storeId, String startDate, String endDate);
}



