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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by order status", example = "PENDING") @RequestParam(required = false) String status,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        // Validate user có quyền truy cập store này
        validateUserStore(currentUser, storeId);
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<OrderResponse> orderPage = orderService.getStoreOrders(storeId, status, pageable);

        return ResponseEntity.ok(ApiResponse.ok(orderPage));
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

    @GetMapping("/store/{storeId}/count-by-status")
    @Operation(summary = "Đếm số lượng đơn hàng theo trạng thái")
    public ResponseEntity<?> countOrdersByStatus(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        Map<String, Long> counts = orderService.countOrdersByStatus(storeId);

        return ResponseEntity.ok(ApiResponse.ok(counts));
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
        orderService.confirmOrder(storeId, orderId);

        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được xác nhận"));
    }

    /**
     * Hủy đơn hàng
     * PUT /api/v1/b2c/orders/{orderId}/cancel?storeId={storeId}
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Hủy đơn hàng", description = "Hủy một đơn hàng và thay đổi trạng thái thành CANCELLED")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "ID of the order to cancel", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String orderId,
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @RequestParam String storeId,
            @Parameter(description = "Lý do hủy đơn", example = "Khách hàng yêu cầu hủy") @RequestParam(required = false) String reason,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        validateUserStore(currentUser, storeId);
        orderService.rejectOrder(storeId, orderId, reason);

        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được hủy"));
    }
}
