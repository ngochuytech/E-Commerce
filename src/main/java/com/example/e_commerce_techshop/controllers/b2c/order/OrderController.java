package com.example.e_commerce_techshop.controllers.b2c.order;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.OrderResponse;
import com.example.e_commerce_techshop.services.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final IOrderService orderService;
    
    // Get orders by store
    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getOrdersByStore(@PathVariable String storeId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable String orderId) {
        try {
            OrderResponse order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(ApiResponse.ok(order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Update order status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        try {
            OrderResponse order = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đơn hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get orders by store and status
    @GetMapping("/store/{storeId}/status/{status}")
    public ResponseEntity<?> getOrdersByStoreAndStatus(@PathVariable String storeId, @PathVariable String status) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByStoreAndStatus(storeId, status);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get recent orders
    @GetMapping("/store/{storeId}/recent")
    public ResponseEntity<?> getRecentOrders(@PathVariable String storeId, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<OrderResponse> orders = orderService.getRecentOrdersByStore(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Cancel order
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId, @RequestParam String reason) {
        try {
            OrderResponse order = orderService.cancelOrder(orderId, reason);
            return ResponseEntity.ok(ApiResponse.ok("Hủy đơn hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get order statistics
    @GetMapping("/store/{storeId}/statistics")
    public ResponseEntity<?> getOrderStatistics(@PathVariable String storeId) {
        try {
            Long pendingCount = orderService.getOrderCountByStoreAndStatus(storeId, "PENDING");
            Long processingCount = orderService.getOrderCountByStoreAndStatus(storeId, "PROCESSING");
            Long shippedCount = orderService.getOrderCountByStoreAndStatus(storeId, "SHIPPED");
            Long deliveredCount = orderService.getOrderCountByStoreAndStatus(storeId, "DELIVERED");
            Long cancelledCount = orderService.getOrderCountByStoreAndStatus(storeId, "CANCELLED");
            
            return ResponseEntity.ok(ApiResponse.ok(
                String.format("PENDING: %d, PROCESSING: %d, SHIPPED: %d, DELIVERED: %d, CANCELLED: %d", 
                    pendingCount, processingCount, shippedCount, deliveredCount, cancelledCount)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Get orders by date range
    @GetMapping("/store/{storeId}/date-range")
    public ResponseEntity<?> getOrdersByDateRange(@PathVariable String storeId, 
                                                  @RequestParam String startDate, 
                                                  @RequestParam String endDate) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByDateRange(storeId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}



