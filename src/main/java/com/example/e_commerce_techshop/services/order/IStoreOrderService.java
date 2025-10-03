package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.b2c.order.OrderDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IStoreOrderService {
    List<OrderDTO> getOrdersByStore(String storeId) throws Exception;
    List<OrderDTO> getOrdersByStoreAndStatus(String storeId, String status) throws Exception;
    List<OrderDTO> getRecentOrdersByStore(String storeId, int limit) throws Exception;
    OrderDTO getOrderById(String orderId) throws Exception;
    OrderDTO updateOrderStatus(String orderId, OrderDTO updateDTO) throws Exception;
    OrderDTO cancelOrder(String orderId, String reason) throws Exception;
    Map<String, Long> getOrderStatistics(String storeId) throws Exception;
    List<OrderDTO> getOrdersByDateRange(String storeId, String startDate, String endDate) throws Exception;
    Page<OrderDTO> getOrdersByStoreWithPagination(String storeId, int page, int size, String status) throws Exception;
}


