package com.example.e_commerce_techshop.controllers.buyer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.annotations.RequireActiveAccount;
import com.example.e_commerce_techshop.dtos.buyer.DisputeRequestDTO;
import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
import com.example.e_commerce_techshop.dtos.buyer.ReturnRequestDTO;
import com.example.e_commerce_techshop.models.Dispute;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.admin.DisputeResponse;
import com.example.e_commerce_techshop.responses.buyer.OrderResponse;
import com.example.e_commerce_techshop.responses.buyer.ReturnRequestResponse;
import com.example.e_commerce_techshop.services.order.IOrderService;
import com.example.e_commerce_techshop.services.returnrequest.IReturnRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/buyer/orders")
@RequiredArgsConstructor
@RequireActiveAccount
@Tag(name = "Buyer Order Management", description = "APIs for buyer order operations")
@SecurityRequirement(name = "bearerAuth")
public class BuyerOrderController {

    private final IOrderService orderService;
    private final IReturnRequestService returnRequestService;

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
            @Parameter(description = "Filter by order status (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, COMPLETED)", example = "PENDING") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orderPage = orderService.getOrderHistory(currentUser, status, pageable);
        Page<OrderResponse> orderResponsePage = orderPage.map(OrderResponse::fromOrder);

        return ResponseEntity.ok(ApiResponse.ok(orderResponsePage));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order detail", description = "Retrieve detailed information of a specific order")
    public ResponseEntity<?> getOrderDetail(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        Order order = orderService.getOrderDetail(currentUser, orderId);
        OrderResponse orderResponse = OrderResponse.fromOrder(order);
        return ResponseEntity.ok(ApiResponse.ok(orderResponse));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only allowed for PENDING status)")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @Parameter(description = "Cancellation reason (optional)") @RequestBody(required = false) String reason,
            @AuthenticationPrincipal User currentUser) throws Exception {
        orderService.cancelOrder(currentUser, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được hủy"));
    }

    @PutMapping("/{orderId}/complete")
    @Operation(summary = "Xác nhận hoàn tất đơn hàng", description = "Buyer xác nhận đã nhận hàng và hoàn tất đơn hàng (chỉ cho đơn hàng DELIVERED)")
    public ResponseEntity<?> completeOrder(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        orderService.completeOrder(currentUser, orderId);
        return ResponseEntity.ok(ApiResponse.ok("Đơn hàng đã được xác nhận hoàn tất"));
    }

    @PostMapping(value = "/{orderId}/return", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Yêu cầu trả hàng", description = "Tạo yêu cầu trả hàng cho đơn hàng đã giao (DELIVERED). Upload ảnh/video minh chứng.")
    public ResponseEntity<?> returnOrder(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @Parameter(description = "Lý do trả hàng") @RequestParam String reason,
            @Parameter(description = "Mô tả chi tiết") @RequestParam(required = false) String description,
            @Parameter(description = "Ảnh/video minh chứng (tối đa 5 file)") @RequestParam(value = "evidenceFiles", required = false) List<MultipartFile> evidenceFiles,
            @AuthenticationPrincipal User currentUser) throws Exception {
        
        ReturnRequestDTO dto = ReturnRequestDTO.builder()
                .reason(reason)
                .description(description)
                .build();
        
        ReturnRequest returnRequest = returnRequestService.createReturnRequest(currentUser, orderId, dto, evidenceFiles);
        return ResponseEntity.ok(ApiResponse.ok(ReturnRequestResponse.fromReturnRequest(returnRequest)));
    }

    @GetMapping("/returns")
    @Operation(summary = "Danh sách yêu cầu trả hàng", description = "Lấy danh sách yêu cầu trả hàng của buyer")
    public ResponseEntity<?> getReturnRequests(
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) throws Exception {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReturnRequest> returnRequests = returnRequestService.getBuyerReturnRequests(currentUser, status, pageable);
        Page<ReturnRequestResponse> responsePage = returnRequests.map(ReturnRequestResponse::fromReturnRequest);
        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }

    @GetMapping("/returns/{returnRequestId}")
    @Operation(summary = "Chi tiết yêu cầu trả hàng", description = "Xem chi tiết yêu cầu trả hàng")
    public ResponseEntity<?> getReturnRequestDetail(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        
        ReturnRequest returnRequest = returnRequestService.getReturnRequestDetail(currentUser, returnRequestId);
        return ResponseEntity.ok(ApiResponse.ok(ReturnRequestResponse.fromReturnRequest(returnRequest)));
    }

    @PostMapping(value = "/returns/{returnRequestId}/dispute", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Khiếu nại", description = "Tạo khiếu nại khi store từ chối yêu cầu trả hàng. Upload ảnh/video minh chứng.")
    public ResponseEntity<?> createDispute(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @Parameter(description = "Nội dung khiếu nại") @RequestParam String content,
            @Parameter(description = "Ảnh/video đính kèm (tối đa 5 file)") @RequestParam(value = "attachmentFiles", required = false) List<MultipartFile> attachmentFiles,
            @AuthenticationPrincipal User currentUser) throws Exception {
    
        
        DisputeRequestDTO dto = DisputeRequestDTO.builder()
                .content(content)
                .build();
        
        Dispute dispute = returnRequestService.createDispute(currentUser, returnRequestId, dto, attachmentFiles);
        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @PostMapping(value = "/disputes/{disputeId}/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Thêm tin nhắn vào khiếu nại", description = "Buyer thêm tin nhắn/bằng chứng vào khiếu nại. Upload ảnh/video đính kèm.")
    public ResponseEntity<?> addDisputeMessage(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @Parameter(description = "Nội dung tin nhắn") @RequestParam String content,
            @Parameter(description = "Ảnh/video đính kèm (tối đa 5 file)") @RequestParam(value = "attachmentFiles", required = false) List<MultipartFile> attachmentFiles,
            @AuthenticationPrincipal User currentUser) throws Exception {
        
        // Upload attachment files to Cloudinary
        
        DisputeRequestDTO dto = DisputeRequestDTO.builder()
                .content(content)
                .build();
        
        Dispute dispute = returnRequestService.addDisputeMessage(currentUser, disputeId, dto, attachmentFiles);
        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @GetMapping("/disputes")
    @Operation(summary = "Danh sách khiếu nại", description = "Lấy danh sách khiếu nại của buyer")
    public ResponseEntity<?> getDisputes(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) throws Exception {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Dispute> disputes = returnRequestService.getBuyerDisputes(currentUser, pageable);
        Page<DisputeResponse> responsePage = disputes.map(DisputeResponse::fromDispute);
        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }
}