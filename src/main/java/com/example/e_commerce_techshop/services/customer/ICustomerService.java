package com.example.e_commerce_techshop.services.customer;

import com.example.e_commerce_techshop.responses.CustomerResponse;
import com.example.e_commerce_techshop.responses.OrderResponse;

import java.util.List;

public interface ICustomerService {
    
    // Get customers by store
    List<CustomerResponse> getCustomersByStore(String storeId);
    
    // Get customer by ID
    CustomerResponse getCustomerById(String customerId);
    
    // Get customer orders
    List<OrderResponse> getCustomerOrders(String customerId, String storeId);
    
    // Get customer statistics
    CustomerResponse getCustomerStatistics(String customerId, String storeId);
    
    // Search customers
    List<CustomerResponse> searchCustomersByStore(String storeId, String keyword);
    
    // Get top customers by spending
    List<CustomerResponse> getTopCustomersBySpending(String storeId, int limit);
    
    // Get new customers
    List<CustomerResponse> getNewCustomers(String storeId, int days);
}



