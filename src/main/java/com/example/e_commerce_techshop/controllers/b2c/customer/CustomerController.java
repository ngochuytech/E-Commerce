package com.example.e_commerce_techshop.controllers.b2c.customer;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.CustomerResponse;
import com.example.e_commerce_techshop.responses.OrderResponse;
import com.example.e_commerce_techshop.services.customer.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final ICustomerService customerService;
    
    // Get customers by store
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getCustomersByStore(@PathVariable String storeId) {
        try {
            List<CustomerResponse> customers = customerService.getCustomersByStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok(customers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get customer by ID
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomerById(@PathVariable String customerId) {
        try {
            CustomerResponse customer = customerService.getCustomerById(customerId);
            return ResponseEntity.ok(ApiResponse.ok(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get customer orders
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<?> getCustomerOrders(@PathVariable String customerId, @RequestParam(required = false) String storeId) {
        try {
            List<OrderResponse> orders = customerService.getCustomerOrders(customerId, storeId);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get customer statistics
    @GetMapping("/{customerId}/statistics")
    public ResponseEntity<?> getCustomerStatistics(@PathVariable String customerId, @RequestParam String storeId) {
        try {
            CustomerResponse customer = customerService.getCustomerStatistics(customerId, storeId);
            return ResponseEntity.ok(ApiResponse.ok(customer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Search customers
    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<?> searchCustomers(@PathVariable String storeId, @RequestParam String keyword) {
        try {
            List<CustomerResponse> customers = customerService.searchCustomersByStore(storeId, keyword);
            return ResponseEntity.ok(ApiResponse.ok(customers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get top customers by spending
    @GetMapping("/store/{storeId}/top-spenders")
    public ResponseEntity<?> getTopCustomersBySpending(@PathVariable String storeId, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<CustomerResponse> customers = customerService.getTopCustomersBySpending(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok(customers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get new customers
    @GetMapping("/store/{storeId}/new")
    public ResponseEntity<?> getNewCustomers(@PathVariable String storeId, @RequestParam(defaultValue = "30") int days) {
        try {
            List<CustomerResponse> customers = customerService.getNewCustomers(storeId, days);
            return ResponseEntity.ok(ApiResponse.ok(customers));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

