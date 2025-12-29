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
import com.example.e_commerce_techshop.responses.ShipmentResponse;
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
@Tag(name = "Buyer Order Management", description = "API cho quản lý đơn hàng của người mua - Xử lý tạo đơn hàng, xem lịch sử, hủy đơn, hoàn tất và trả hàng")
@SecurityRequirement(name = "bearerAuth")
public class BuyerOrderController {

    private final IOrderService orderService;
    private final IReturnRequestService returnRequestService;

    @GetMapping("")
    @Operation(summary = "Lấy lịch sử đơn hàng", description = "Lấy danh sách phân trang các đơn hàng của người dùng với tùy chọn lọc theo trạng thái")
    public ResponseEntity<?> getOrderHistory(
            @Parameter(description = "Lọc theo trạng thái đơn hàng (PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, COMPLETED)", example = "PENDING") @RequestParam(required = false) String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir,
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
    @Operation(summary = "Lấy chi tiết đơn hàng", description = "Lấy thông tin chi tiết của một đơn hàng cụ thể")
    public ResponseEntity<?> getOrderDetail(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        Order order = orderService.getOrderDetail(currentUser, orderId);
        OrderResponse orderResponse = OrderResponse.fromOrder(order);

        // Thêm thông tin hoàn tiền (nếu có)
        OrderResponse.RefundInfo refundInfo = orderService.getOrderRefundInfo(currentUser, orderId);
        orderResponse.setRefundInfo(refundInfo);

        // Thêm thông tin ngân hàng từ ReturnRequest (nếu có)
        if (order.getReturnRequestId() != null) {
            try {
                ReturnRequest returnRequest = returnRequestService.getReturnRequestDetail(currentUser,
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

    @GetMapping("/returns")
    @Operation(summary = "Danh sách yêu cầu trả hàng", description = "Lấy danh sách yêu cầu trả hàng của buyer (bao gồm disputes liên quan)")
    public ResponseEntity<?> getReturnRequests(
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReturnRequest> returnRequests = returnRequestService.getBuyerReturnRequests(currentUser, status, pageable);

        // Map và thêm disputes cho từng return request
        Page<ReturnRequestResponse> responsePage = returnRequests.map(returnRequest -> {
            try {
                List<Dispute> disputes = returnRequestService.getDisputesByReturnRequest(returnRequest.getId());
                return ReturnRequestResponse.fromReturnRequestWithDisputes(returnRequest, disputes);
            } catch (Exception e) {
                // Nếu có lỗi khi lấy disputes, vẫn trả về return request nhưng không có
                // disputes
                return ReturnRequestResponse.fromReturnRequest(returnRequest);
            }
        });

        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }

    @GetMapping("/returns/{returnRequestId}")
    @Operation(summary = "Chi tiết yêu cầu trả hàng", description = "Xem chi tiết yêu cầu trả hàng (bao gồm disputes liên quan)")
    public ResponseEntity<?> getReturnRequestDetail(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        ReturnRequest returnRequest = returnRequestService.getReturnRequestDetail(currentUser, returnRequestId);
        List<Dispute> disputes = returnRequestService.getDisputesByReturnRequest(returnRequestId);

        return ResponseEntity.ok(ApiResponse.ok(
                ReturnRequestResponse.fromReturnRequestWithDisputes(returnRequest, disputes)));
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

    @GetMapping("/disputes/{disputeId}")
    @Operation(summary = "Chi tiết khiếu nại", description = "Xem chi tiết một khiếu nại cụ thể")
    public ResponseEntity<?> getDisputeDetail(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Dispute dispute = returnRequestService.getDisputeDetail(disputeId);

        // Kiểm tra dispute có thuộc về buyer này không
        if (!dispute.getBuyer().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem khiếu nại này");
        }

        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @GetMapping("/{orderId}/refund-status")
    @Operation(summary = "Thông tin hoàn tiền", description = "Lấy thông tin hoàn tiền của đơn hàng (nếu có). Trả về null nếu chưa có hoàn tiền.")
    public ResponseEntity<?> getOrderRefundStatus(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        OrderResponse.RefundInfo refundInfo = orderService.getOrderRefundInfo(currentUser, orderId);
        return ResponseEntity.ok(ApiResponse.ok(refundInfo));
    }

    @GetMapping("/{orderId}/return-shipment")
    @Operation(summary = "Thông tin vận chuyển trả hàng", description = "Lấy thông tin vận chuyển của đơn hàng trả về (nếu có). Trả về null nếu chưa có shipment trả hàng.")
    public ResponseEntity<?> getReturnShipmentInfo(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        ShipmentResponse response = orderService.getReturnShipmentInfo(currentUser, orderId);

        if (response == null) {
            return ResponseEntity.ok(ApiResponse.ok("Chưa có thông tin vận chuyển trả hàng"));
        }

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Thanh toán và tạo đơn hàng", description = "Xử lý các mặt hàng đã chọn trong giỏ hàng và tạo đơn hàng được nhóm theo cửa hàng")
    public ResponseEntity<?> checkout(
            @Parameter(description = "Thông tin đơn hàng bao gồm các mặt hàng đã chọn, địa chỉ giao hàng và phương thức thanh toán") @Valid @RequestBody OrderDTO orderDTO,
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

    @PostMapping(value = "/{orderId}/return", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Yêu cầu trả hàng", description = "Tạo yêu cầu trả hàng cho đơn hàng đã giao (DELIVERED). Upload ảnh/video minh chứng. Đơn COD bắt buộc nhập thông tin ngân hàng.")
    public ResponseEntity<?> returnOrder(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @Parameter(description = "Lý do trả hàng") @RequestParam String reason,
            @Parameter(description = "Mô tả chi tiết") @RequestParam(required = false) String description,
            @Parameter(description = "Ảnh/video minh chứng (tối đa 5 file)") @RequestParam(value = "evidenceFiles", required = false) List<MultipartFile> evidenceFiles,
            @Parameter(description = "Tên ngân hàng (bắt buộc cho COD)") @RequestParam(required = false) String bankName,
            @Parameter(description = "Số tài khoản ngân hàng (bắt buộc cho COD)") @RequestParam(required = false) String bankAccountNumber,
            @Parameter(description = "Tên chủ tài khoản (bắt buộc cho COD)") @RequestParam(required = false) String bankAccountName,
            @AuthenticationPrincipal User currentUser) throws Exception {

        ReturnRequestDTO dto = ReturnRequestDTO.builder()
                .reason(reason)
                .description(description)
                .bankName(bankName)
                .bankAccountNumber(bankAccountNumber)
                .bankAccountName(bankAccountName)
                .build();

        ReturnRequest returnRequest = returnRequestService.createReturnRequest(currentUser, orderId, dto,
                evidenceFiles);
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

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Hủy đơn hàng", description = "Hủy đơn hàng (chỉ cho trạng thái PENDING)")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "Order ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String orderId,
            @Parameter(description = "Lý do hủy (tùy chọn)") @RequestBody(required = false) String reason,
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

    @PutMapping("/returns/{returnRequestId}/cancel")
    @Operation(summary = "Hủy yêu cầu trả hàng", description = "Buyer hủy yêu cầu trả hàng (chỉ được hủy khi status là PENDING hoặc REJECTED)")
    public ResponseEntity<?> cancelReturnRequest(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        ReturnRequest returnRequest = returnRequestService.cancelReturnRequest(currentUser, returnRequestId);
        return ResponseEntity.ok(ApiResponse.ok(ReturnRequestResponse.fromReturnRequest(returnRequest)));
    }
}