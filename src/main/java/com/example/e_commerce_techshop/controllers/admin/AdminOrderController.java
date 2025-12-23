package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.OrderResponse;
import com.example.e_commerce_techshop.services.order.IOrderService;
import com.example.e_commerce_techshop.services.returnrequest.IReturnRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Order Management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    private final IOrderService orderService;
    private final IReturnRequestService returnRequestService;

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order detail", description = "Retrieve detailed information of a specific order")
    public ResponseEntity<?> getOrderDetail(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId) throws Exception {
        Order order = orderService.getOrderById(orderId);
        OrderResponse orderResponse = OrderResponse.fromOrder(order);

        // Thêm thông tin hoàn tiền (nếu có)
        OrderResponse.RefundInfo refundInfo = orderService.getOrderRefundInfo(order.getBuyer(), orderId);
        orderResponse.setRefundInfo(refundInfo);

        // Thêm thông tin ngân hàng từ ReturnRequest (nếu có)
        if (order.getReturnRequestId() != null) {
            try {
                ReturnRequest returnRequest = returnRequestService.getReturnRequestDetail(order.getBuyer(),
                        order.getReturnRequestId());
                orderResponse.setBankName(returnRequest.getBankName());
                orderResponse.setBankAccountNumber(returnRequest.getBankAccountNumber());
                orderResponse.setBankAccountName(returnRequest.getBankAccountName());
            } catch (Exception e) {
                // Nếu không lấy được returnRequest, các trường bank sẽ là null
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(orderResponse));
    }
}
