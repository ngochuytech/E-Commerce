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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Checkout and create order", 
               description = "Process cart items and create orders grouped by store")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Checkout successful",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid data or business logic error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> checkout(
            @Parameter(description = "Order information including delivery address and payment method")
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
    @Operation(summary = "Get order history", 
               description = "Retrieve paginated list of user's orders with optional status filtering")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Order history retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getOrderHistory(
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filter by order status (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc")
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
    @Operation(summary = "Get order detail", 
               description = "Retrieve detailed information of a specific order")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Order detail retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Order not found or access denied",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getOrderDetail(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
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
    @Operation(summary = "Cancel order", 
               description = "Cancel an order (only allowed for PENDING status)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Cannot cancel order (invalid status or not found)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String orderId,
            @Parameter(description = "Cancellation reason (optional)")
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