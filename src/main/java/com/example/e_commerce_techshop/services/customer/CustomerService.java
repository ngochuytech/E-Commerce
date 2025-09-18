package com.example.e_commerce_techshop.services.customer;

import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.UserRepository;
import com.example.e_commerce_techshop.responses.CustomerResponse;
import com.example.e_commerce_techshop.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService {
    
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
    @Override
    public List<CustomerResponse> getCustomersByStore(String storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        List<String> customerIds = orders.stream()
                .map(Order::getBuyerId)
                .distinct()
                .collect(Collectors.toList());
        
        List<User> customers = userRepository.findAllById(customerIds);
        return customers.stream()
                .map(CustomerResponse::fromUser)
                .collect(Collectors.toList());
    }
    
    @Override
    public CustomerResponse getCustomerById(String customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));
        
        return CustomerResponse.fromUser(customer);
    }
    
    @Override
    public List<OrderResponse> getCustomerOrders(String customerId, String storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId).stream()
                .filter(order -> order.getBuyerId().equals(customerId))
                .collect(Collectors.toList());
        
        return orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    @Override
    public CustomerResponse getCustomerStatistics(String customerId, String storeId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));
        
        List<Order> customerOrders = orderRepository.findByStoreId(storeId).stream()
                .filter(order -> order.getBuyerId().equals(customerId))
                .collect(Collectors.toList());
        
        int totalOrders = customerOrders.size();
        BigDecimal totalSpent = customerOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        LocalDateTime lastOrderDate = customerOrders.stream()
                .map(Order::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        CustomerResponse response = CustomerResponse.fromUser(customer);
        response.setTotalOrders(totalOrders);
        response.setTotalSpent(totalSpent);
        response.setLastOrderDate(lastOrderDate);
        
        return response;
    }
    
    @Override
    public List<CustomerResponse> searchCustomersByStore(String storeId, String keyword) {
        List<CustomerResponse> customers = getCustomersByStore(storeId);
        return customers.stream()
                .filter(customer -> 
                    customer.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                    customer.getEmail().toLowerCase().contains(keyword.toLowerCase()) ||
                    customer.getPhone().contains(keyword))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CustomerResponse> getTopCustomersBySpending(String storeId, int limit) {
        List<CustomerResponse> customers = getCustomersByStore(storeId);
        return customers.stream()
                .map(customer -> getCustomerStatistics(customer.getId(), storeId))
                .sorted((c1, c2) -> c2.getTotalSpent().compareTo(c1.getTotalSpent()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CustomerResponse> getNewCustomers(String storeId, int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<CustomerResponse> customers = getCustomersByStore(storeId);
        
        return customers.stream()
                .filter(customer -> customer.getCreatedAt().isAfter(cutoffDate))
                .collect(Collectors.toList());
    }
}



