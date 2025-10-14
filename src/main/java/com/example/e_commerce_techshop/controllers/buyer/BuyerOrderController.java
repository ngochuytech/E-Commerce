package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.dtos.OrderDTO;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/buyer/orders")
@RequiredArgsConstructor
public class BuyerOrderController {

    private final IOrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result,
            @AuthenticationPrincipal User currentUser) {
        
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Dữ liệu không hợp lệ")
            );
        }
        
        try {
            List<Order> orderResponses = orderService.checkout(currentUser.getEmail(), orderDTO);
            List<OrderResponse> orderResponseList = orderResponses.stream()
                    .map(OrderResponse::fromOrder)
                    .toList();

            return ResponseEntity.ok(ApiResponse.ok(orderResponseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getOrderHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            Page<Order> orderPage = orderService.getOrderHistory(currentUser.getEmail(), page, size, status);
            Page<OrderResponse> orderResponsePage = orderPage.map(OrderResponse::fromOrder);
            
            return ResponseEntity.ok(orderResponsePage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(
            @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Order order = orderService.getOrderDetail(currentUser.getEmail(), orderId);
            return ResponseEntity.ok(ApiResponse.ok(OrderResponse.fromOrder(order)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) String reason,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            orderService.cancelOrder(currentUser.getEmail(), orderId);
            return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được hủy"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
}