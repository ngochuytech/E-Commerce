package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.b2c.order.OrderDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    
    private final OrderRepository orderRepository;
    
    @Override
    public OrderResponse createOrder(OrderDTO orderDTO) {
        Order order = Order.builder()
                .buyerId(orderDTO.getBuyerId())
                .storeId(orderDTO.getStoreId())
                .promotionId(orderDTO.getPromotionId())
                .totalPrice(orderDTO.getTotalPrice())
                .addressId(orderDTO.getAddressId())
                .paymentMethod(orderDTO.getPaymentMethod())
                .status(orderDTO.getStatus() != null ? orderDTO.getStatus() : "PENDING")
                .build();
        
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(savedOrder);
    }
    
    @Override
    public OrderResponse updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(updatedOrder);
    }
    
    @Override
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        return OrderResponse.fromOrder(order);
    }
    
    @Override
    public List<OrderResponse> getOrdersByStore(String storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        return orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrderResponse> getOrdersByStoreAndStatus(String storeId, String status) {
        List<Order> orders = orderRepository.findByStoreIdAndStatus(storeId, status);
        return orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrderResponse> getRecentOrdersByStore(String storeId, int limit) {
        List<Order> orders = orderRepository.findRecentOrdersByStoreId(storeId);
        return orders.stream()
                .limit(limit)
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    @Override
    public OrderResponse cancelOrder(String orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        order.setStatus("CANCELLED");
        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(updatedOrder);
    }
    
    @Override
    public Long getOrderCountByStoreAndStatus(String storeId, String status) {
        return orderRepository.countByStoreIdAndStatus(storeId, status);
    }
    
    @Override
    public List<OrderResponse> getOrdersByDateRange(String storeId, String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, start, end);
        return orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
}



