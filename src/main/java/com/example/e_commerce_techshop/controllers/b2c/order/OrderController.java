package com.example.e_commerce_techshop.controllers.b2c.order;

import com.example.e_commerce_techshop.dtos.b2c.order.OrderResponse;
import com.example.e_commerce_techshop.dtos.b2c.order.OrderSummaryResponse;
import com.example.e_commerce_techshop.dtos.b2c.order.UpdateOrderStatusDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.order.IStoreOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/b2c/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IStoreOrderService sellerOrderService;

    private String getCurrentStoreOwnerEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return authentication.getName(); // Returns email
    }

    /**
     * Lấy tất cả orders của store
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getOrdersByStore(@PathVariable String storeId) {
        try {
            List<OrderSummaryResponse> orders = sellerOrderService.getOrdersByStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết order theo ID
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String orderId) {
        try {
            OrderResponse order = sellerOrderService.getOrderById(orderId);
            return ResponseEntity.ok(ApiResponse.ok(order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật trạng thái order
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusDTO updateDTO,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Dữ liệu không hợp lệ"));
        }
        
        try {
            OrderResponse order = sellerOrderService.updateOrderStatus(orderId, updateDTO);
            return ResponseEntity.ok(ApiResponse.ok(order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy orders theo store và status
     */
    @GetMapping("/store/{storeId}/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStoreAndStatus(
            @PathVariable String storeId, 
            @PathVariable String status) {
        try {
            List<OrderResponse> orders = sellerOrderService.getOrdersByStoreAndStatus(storeId, status);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy orders gần đây của store
     */
    @GetMapping("/store/{storeId}/recent")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getRecentOrders(
            @PathVariable String storeId, 
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<OrderResponse> orders = sellerOrderService.getRecentOrdersByStore(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Hủy order
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String orderId, 
            @RequestParam(required = false) String reason) {
        try {
            OrderResponse order = sellerOrderService.cancelOrder(orderId, reason);
            return ResponseEntity.ok(ApiResponse.ok(order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy thống kê orders theo store
     */
    @GetMapping("/store/{storeId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getOrderStatistics(@PathVariable String storeId) {
        try {
            Map<String, Long> statistics = sellerOrderService.getOrderStatistics(storeId);
            return ResponseEntity.ok(ApiResponse.ok(statistics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy orders theo khoảng thời gian
     */
    @GetMapping("/store/{storeId}/date-range")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByDateRange(
            @PathVariable String storeId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<OrderResponse> orders = sellerOrderService.getOrdersByDateRange(storeId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.ok(orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy orders theo store với phân trang
     */
    @GetMapping("/store/{storeId}/paginated")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getOrdersByStoreWithPagination(
            @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            Page<OrderSummaryResponse> ordersPage = sellerOrderService.getOrdersByStoreWithPagination(storeId, page, size, status);
            return ResponseEntity.ok(ApiResponse.ok(ordersPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
