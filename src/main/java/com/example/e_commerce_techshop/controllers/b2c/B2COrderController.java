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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/b2c/orders")
@RequiredArgsConstructor
@Tag(name = "B2C Order Management", description = "Order management APIs for B2C stores - Handle order processing, status updates, and order analytics")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Get store orders", description = "Retrieve paginated list of orders for a specific store with optional status filtering")
    public ResponseEntity<?> getStoreOrders(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(description = "Page number (1-based)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by order status", example = "PENDING") @RequestParam(required = false) String status,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        // Validate user có quyền truy cập store này
        validateUserStore(currentUser, storeId);

        Page<Order> orderPage = orderService.getStoreOrders(storeId, page, size, status);
        Page<OrderResponse> orderResponsePage = orderPage.map(OrderResponse::fromOrder);

        return ResponseEntity.ok(ApiResponse.ok(orderResponsePage));
    }

    /**
     * Lấy chi tiết đơn hàng của store
     * GET /api/v1/b2c/orders/{orderId}?storeId={storeId}
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get store order detail", description = "Retrieve detailed information of a specific order for a store")
    public ResponseEntity<?> getStoreOrderDetail(
            @Parameter(description = "ID of the order", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String orderId,
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        Order order = orderService.getStoreOrderDetail(storeId, orderId);

        return ResponseEntity.ok(ApiResponse.ok(OrderResponse.fromOrder(order)));
    }

    /**
     * Cập nhật trạng thái đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/status?storeId={storeId}
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)")
    public ResponseEntity<?> updateOrderStatus(
            @Parameter(description = "ID of the order", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String orderId,
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(description = "New status for the order", required = true, example = "CONFIRMED") @RequestParam String status,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        Order updatedOrder = orderService.updateOrderStatus(storeId, orderId, status);

        return ResponseEntity.ok(ApiResponse.ok(OrderResponse.fromOrder(updatedOrder)));
    }

    /**
     * Xác nhận đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/confirm?storeId={storeId}
     */
    @PutMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm order", description = "Confirm a pending order and change status to CONFIRMED")
    public ResponseEntity<?> confirmOrder(
            @Parameter(description = "ID of the order to confirm", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String orderId,
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        orderService.updateOrderStatus(storeId, orderId, "CONFIRMED");

        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được xác nhận"));
    }

    /**
     * Chuyển đơn hàng sang trạng thái đang giao
     * PUT /api/v1/b2c/orders/{orderId}/ship?storeId={storeId}
     */
    @PutMapping("/{orderId}/ship")
    @Operation(summary = "Ship order", description = "Mark order as shipped and change status to SHIPPING")
    public ResponseEntity<?> shipOrder(
            @Parameter(description = "ID of the order to ship", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String orderId,
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        orderService.updateOrderStatus(storeId, orderId, "SHIPPING");

        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã chuyển sang trạng thái đang giao"));
    }

    /**
     * Hoàn thành đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/deliver?storeId={storeId}
     */
    @PutMapping("/{orderId}/deliver")
    @Operation(summary = "Deliver order", description = "Mark order as delivered and change status to DELIVERED")
    public ResponseEntity<?> deliverOrder(
            @Parameter(description = "ID of the order to mark as delivered", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String orderId,
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        orderService.updateOrderStatus(storeId, orderId, "DELIVERED");

        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được giao thành công"));
    }

    /**
     * Hủy đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/cancel?storeId={storeId}
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order and change status to CANCELLED with optional reason")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "ID of the order to cancel", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String orderId,
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(description = "Reason for cancellation", example = "Khách hàng yêu cầu hủy") @RequestParam(required = false) String reason,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        orderService.updateOrderStatus(storeId, orderId, "CANCELLED");

        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được hủy"));
    }

    /**
     * Thống kê đơn hàng của store
     * GET /api/v1/b2c/orders/statistics?storeId={storeId}
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Retrieve comprehensive order statistics for a store including counts by status, trends, and performance metrics")
    public ResponseEntity<?> getOrderStatistics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        Map<String, Object> stats = orderService.getStoreOrderStatistics(storeId);

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    /**
     * Thống kê doanh thu theo khoảng thời gian
     * GET /api/v1/b2c/orders/revenue?storeId={storeId}
     */
    @GetMapping("/revenue")
    @Operation(summary = "Get revenue statistics", description = "Retrieve revenue statistics for a store within a specific date range")
    public ResponseEntity<?> getRevenueStatistics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2024-01-01") @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2024-12-31") @RequestParam String endDate,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        Map<String, Object> revenue = orderService.getStoreRevenue(storeId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.ok(revenue));
    }
}
