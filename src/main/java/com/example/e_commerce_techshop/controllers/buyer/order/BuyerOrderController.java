package com.example.e_commerce_techshop.controllers.buyer.order;

import com.example.e_commerce_techshop.dtos.buyer.order.OrderDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.OrderResponse;
import com.example.e_commerce_techshop.services.order.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/buyer/orders")
@RequiredArgsConstructor
public class BuyerOrderController {

    private final IOrderService orderService;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return authentication.getName(); // Returns email
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result) {
        
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Dữ liệu không hợp lệ")
            );
        }
        
        try {
            String userEmail = getCurrentUserEmail();
            List<OrderResponse> orderResponses = orderService.checkout(userEmail, orderDTO);

            return ResponseEntity.ok(ApiResponse.ok(orderResponses));
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
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            String userEmail = getCurrentUserEmail();
            Page<OrderResponse> orderPage = orderService.getOrderHistory(userEmail, page, size, status);
            
            return ResponseEntity.ok(ApiResponse.ok(orderPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable String orderId) {
        try {
            String userEmail = getCurrentUserEmail();
            OrderResponse orderDetail = orderService.getOrderDetail(userEmail, orderId);
            return ResponseEntity.ok(ApiResponse.ok(orderDetail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable String orderId,
            @RequestBody(required = false) String reason
    ) {
        try {
            String userEmail = getCurrentUserEmail();
            OrderResponse cancelledOrder = orderService.cancelOrder(userEmail, orderId);
            return ResponseEntity.ok(ApiResponse.ok(cancelledOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
}