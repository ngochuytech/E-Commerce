package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.OrderResponse;
import com.example.e_commerce_techshop.services.order.IOrderService;
import com.example.e_commerce_techshop.services.store.IStoreService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/b2c/orders")
@RequiredArgsConstructor
public class B2COrderController {
    
    private final IOrderService orderService;
    private final IStoreService storeService;

    /**
     * Helper method để validate store thuộc về user
     */
    private void validateUserStore(User currentUser, String storeId) {
        List<Store> userStores = storeService.getStoresByOwner(currentUser.getId());
        boolean hasStore = userStores.stream()
            .anyMatch(store -> store.getId().equals(storeId));
        
        if (!hasStore) {
            throw new RuntimeException("Bạn không có quyền truy cập cửa hàng này");
        }
    }
    
    /**
     * Lấy danh sách đơn hàng của store
     * GET /api/v1/b2c/orders?storeId={storeId}
     */
    @GetMapping("")
    public ResponseEntity<?> getStoreOrders(
            @RequestParam String storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal User currentUser) {
        try {
            // Validate user có quyền truy cập store này
            validateUserStore(currentUser, storeId);
            
            Page<Order> orderPage = orderService.getStoreOrders(storeId, page, size, status);
            Page<OrderResponse> orderResponsePage = orderPage.map(OrderResponse::fromOrder);
            
            return ResponseEntity.ok(ApiResponse.ok(orderResponsePage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Lấy chi tiết đơn hàng của store
     * GET /api/v1/b2c/orders/{orderId}?storeId={storeId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getStoreOrderDetail(
            @PathVariable String orderId,
            @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            Order order = orderService.getStoreOrderDetail(storeId, orderId);
            
            return ResponseEntity.ok(ApiResponse.ok(OrderResponse.fromOrder(order)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Cập nhật trạng thái đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/status?storeId={storeId}
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String storeId,
            @RequestParam String status,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            Order updatedOrder = orderService.updateOrderStatus(storeId, orderId, status);
            
            return ResponseEntity.ok(ApiResponse.ok(OrderResponse.fromOrder(updatedOrder)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Xác nhận đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/confirm?storeId={storeId}
     */
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(
            @PathVariable String orderId,
            @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            orderService.updateOrderStatus(storeId, orderId, "CONFIRMED");
            
            return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được xác nhận"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Chuyển đơn hàng sang trạng thái đang giao
     * PUT /api/v1/b2c/orders/{orderId}/ship?storeId={storeId}
     */
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<?> shipOrder(
            @PathVariable String orderId,
            @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            orderService.updateOrderStatus(storeId, orderId, "SHIPPING");
            
            return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã chuyển sang trạng thái đang giao"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Hoàn thành đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/deliver?storeId={storeId}
     */
    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<?> deliverOrder(
            @PathVariable String orderId,
            @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            orderService.updateOrderStatus(storeId, orderId, "DELIVERED");
            
            return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được giao thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Hủy đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/cancel?storeId={storeId}
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable String orderId,
            @RequestParam String storeId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            orderService.updateOrderStatus(storeId, orderId, "CANCELLED");
            
            return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được hủy"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Thống kê đơn hàng của store
     * GET /api/v1/b2c/orders/statistics?storeId={storeId}
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getOrderStatistics(
            @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            Map<String, Object> stats = orderService.getStoreOrderStatistics(storeId);
            
            return ResponseEntity.ok(ApiResponse.ok(stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * Thống kê doanh thu theo khoảng thời gian
     * GET /api/v1/b2c/orders/revenue?storeId={storeId}
     */
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueStatistics(
            @RequestParam String storeId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @AuthenticationPrincipal User currentUser) {
        try {
            validateUserStore(currentUser, storeId);
            Map<String, Object> revenue = orderService.getStoreRevenue(storeId, startDate, endDate);
            
            return ResponseEntity.ok(ApiResponse.ok(revenue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
}
