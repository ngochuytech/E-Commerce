package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.OrderResponse;
import com.example.e_commerce_techshop.services.order.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/buyer/orders")
@RequiredArgsConstructor
@Tag(name = "Buyer Order Management", description = "APIs for buyer order operations")
@SecurityRequirement(name = "bearerAuth")
public class BuyerOrderController {

    private final IOrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout and create order", description = "Process selected cart items and create orders grouped by store")
    public ResponseEntity<?> checkout(
            @Parameter(description = "Order information including selected items, delivery address and payment method") @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result,
            @AuthenticationPrincipal User currentUser) throws Exception {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        List<Order> orderResponses = orderService.checkout(currentUser, orderDTO);
        List<OrderResponse> orderResponseList = orderResponses.stream()
                .map(OrderResponse::fromOrder)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(orderResponseList));
    }

    @GetMapping("")
    @Operation(summary = "Get order history", description = "Retrieve paginated list of user's orders with optional status filtering")
    public ResponseEntity<?> getOrderHistory(
            @Parameter(description = "Page number (1-based)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by order status (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)", example = "PENDING") @RequestParam(required = false) String status,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) throws Exception {
        Page<Order> orderPage = orderService.getOrderHistory(currentUser.getEmail(), page, size, status);
        Page<OrderResponse> orderResponsePage = orderPage.map(OrderResponse::fromOrder);

        return ResponseEntity.ok(orderResponsePage);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order detail", description = "Retrieve detailed information of a specific order")
    public ResponseEntity<?> getOrderDetail(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        Order order = orderService.getOrderDetail(currentUser.getEmail(), orderId);
        return ResponseEntity.ok(ApiResponse.ok(OrderResponse.fromOrder(order)));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only allowed for PENDING status)")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @Parameter(description = "Cancellation reason (optional)") @RequestBody(required = false) String reason,
            @AuthenticationPrincipal User currentUser) throws Exception {
        orderService.cancelOrder(currentUser.getEmail(), orderId);
        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được hủy"));
    }
}