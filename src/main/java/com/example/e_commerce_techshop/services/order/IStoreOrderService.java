package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.b2c.order.OrderResponse;
import com.example.e_commerce_techshop.dtos.b2c.order.OrderSummaryResponse;
import com.example.e_commerce_techshop.dtos.b2c.order.UpdateOrderStatusDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IStoreOrderService {
    List<OrderSummaryResponse> getOrdersByStore(String storeId) throws Exception;
    List<OrderResponse> getOrdersByStoreAndStatus(String storeId, String status) throws Exception;
    List<OrderResponse> getRecentOrdersByStore(String storeId, int limit) throws Exception;
    OrderResponse getOrderById(String orderId) throws Exception;
    OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusDTO updateDTO) throws Exception;
    OrderResponse cancelOrder(String orderId, String reason) throws Exception;
    Map<String, Long> getOrderStatistics(String storeId) throws Exception;
    List<OrderResponse> getOrdersByDateRange(String storeId, String startDate, String endDate) throws Exception;
    Page<OrderSummaryResponse> getOrdersByStoreWithPagination(String storeId, int page, int size, String status) throws Exception;
}


