package com.example.e_commerce_techshop.controllers.buyer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.momo.MomoPaymentRequest;
import com.example.e_commerce_techshop.dtos.momo.MomoRefundRequest;
import com.example.e_commerce_techshop.dtos.vnpay.PaymentDTO;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.dtos.vnpay.PaymentQueryDTO;
import com.example.e_commerce_techshop.dtos.vnpay.PaymentRefundDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.momo.IMomoService;
import com.example.e_commerce_techshop.services.order.IOrderService;
import com.example.e_commerce_techshop.services.vnpay.IVNPayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/buyer/payments")
@Tag(name = "Buyer Payment Management", description = "APIs for buyers to manage payment transactions via VNPay gateway")
public class BuyerPaymentController {
        private final IOrderService orderService;
        private final IVNPayService vnPayService;
        private final IMomoService momoService;

        @PostMapping("/create_payment_url")
        @Operation(summary = "Create VNPay payment URL", description = "Generate a payment URL for order payment via VNPay gateway. User will be redirected to VNPay website.")
        public ResponseEntity<?> createPayment(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payment details including amount, bank code, and language", required = true) @RequestBody PaymentDTO paymentRequest,
                        HttpServletRequest request) throws Exception {
                String paymentUrl = vnPayService.createPaymentUrl(paymentRequest, request);

                return ResponseEntity.ok(ApiResponse.ok(paymentUrl));
        }

        @PostMapping("/query")
        @Operation(summary = "Query payment transaction status", description = "Query the status of a payment transaction from VNPay. Used to verify if payment was successful.")
        public ResponseEntity<?> queryTransaction(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Query details including order ID and transaction date", required = true) @RequestBody PaymentQueryDTO paymentQueryDTO,
                        HttpServletRequest request) throws Exception {
                String result = vnPayService.queryTransaction(paymentQueryDTO, request);
                return ResponseEntity.ok(ApiResponse.ok(result));
        }

        @PostMapping("/refund")
        @Operation(summary = "Refund payment transaction", description = "Request a refund for a completed payment transaction. Must be initiated by admin or authorized person.")
        public ResponseEntity<?> refundTransaction(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refund details including order ID, amount, and transaction date", required = true) @Valid @RequestBody PaymentRefundDTO paymentRefundDTO,
                        BindingResult result) throws Exception {
                if (result.hasErrors()) {
                        List<String> errorMessages = result.getFieldErrors()
                                        .stream()
                                        .map(error -> error.getDefaultMessage())
                                        .toList();
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error(
                                                        "Dữ liệu không hợp lệ: " + String.join(", ", errorMessages)));
                }
                String response = vnPayService.refundTransaction(paymentRefundDTO);
                return ResponseEntity.ok().body(ApiResponse.ok(response));
        }

        @PostMapping("/momo/create_payment_request")
        @Operation(summary = "Create MoMo payment request", description = "Generate a payment request for EXISTING order via MoMo gateway.")
        public ResponseEntity<?> createMomoPaymentRequest(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payment details with orderId and amount", required = true) @Valid @RequestBody MomoPaymentRequest paymentRequest)
                        throws Exception {
                // Kiểm tra đơn hàng có quá 1 giờ không thanh toán
                Order order = orderService.getOrderById(paymentRequest.getOrderId());
                if (order.getCreatedAt() != null) {
                        Duration duration = Duration.between(order.getCreatedAt(), LocalDateTime.now());
                        if (duration.toMinutes() > 60) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.error(
                                                                "Đơn hàng đã quá thời hạn thanh toán (1 giờ). Vui lòng đặt hàng lại."));
                        }
                }

                String result = momoService.createPaymentRequest(
                                paymentRequest.getOrderId(),
                                paymentRequest.getAmount());
                return ResponseEntity.ok(result);
        }

        @GetMapping("/momo/check_status/{orderId}")
        @Operation(summary = "Check MoMo payment status", description = "Query the status of a MoMo payment transaction.")
        public ResponseEntity<?> checkMomoPaymentStatus(
                        @PathVariable String orderId) throws Exception {
                String result = momoService.checkPaymentStatus(orderId);
                return ResponseEntity.ok(result);
        }

        @PostMapping("/momo/refund")
        @Operation(summary = "Refund MoMo payment", description = "Request a refund for a completed MoMo payment transaction. Supports partial and full refund.")
        public ResponseEntity<?> refundMomoPayment(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Refund details including transId, amount and description", required = true) @Valid @RequestBody MomoRefundRequest refundRequest)
                        throws Exception {
                String result = momoService.refundPayment(
                                refundRequest.getTransId(),
                                refundRequest.getAmount(),
                                refundRequest.getDescription());
                return ResponseEntity.ok(result);
        }

        @GetMapping("/momo/refund/check_status/{orderId}")
        @Operation(summary = "Check MoMo refund status", description = "Query the status of a MoMo refund transaction. Use system orderId, will lookup momoRefundOrderId automatically.")
        public ResponseEntity<?> checkMomoRefundStatus(
                        @PathVariable String orderId) throws Exception {
                // Lấy momoRefundOrderId từ database
                Order order = orderService.getOrderById(orderId);
                String momoRefundOrderId = order.getMomoRefundOrderId();
                
                if (momoRefundOrderId == null || momoRefundOrderId.isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("Đơn hàng chưa có giao dịch hoàn tiền MoMo"));
                }
                
                String result = momoService.checkRefundStatus(momoRefundOrderId);
                return ResponseEntity.ok(result);
        }

        @PostMapping("/momo/ipn")
        @Operation(summary = "MoMo IPN callback", description = "Webhook endpoint for MoMo to notify payment results (server-to-server).")
        public ResponseEntity<?> momoIpnCallback(@RequestBody String ipnData, HttpServletRequest request) throws Exception {
                System.out.println("=== MoMo IPN Callback Received ===");
                System.out.println("Request from IP: " + request.getRemoteAddr());
                System.out.println("Request method: " + request.getMethod());
                System.out.println("Content-Type: " + request.getContentType());
                System.out.println("Raw IPN Data: " + ipnData);

                try {
                        // Parse JSON
                        org.cloudinary.json.JSONObject json = new org.cloudinary.json.JSONObject(ipnData);

                        String orderId = json.getString("orderId");
                        int resultCode = json.getInt("resultCode");
                        Long transId = json.optLong("transId", 0L);
                        Long amount = json.getLong("amount");
                        String message = json.getString("message");

                        System.out.println("Parsed - OrderId: " + orderId + ", ResultCode: " + resultCode + ", TransId: " + transId + ", Amount: " + amount);

                        // Cập nhật trạng thái thanh toán của đơn hàng
                        if (resultCode == 0) {
                                // Thanh toán thành công
                                orderService.updatePaymentStatus(orderId, Order.PaymentStatus.PAID.name(), transId);
                                System.out.println("✓ Payment successful for order: " + orderId);
                        } else {
                                // Thanh toán thất bại
                                orderService.updatePaymentStatus(orderId, Order.PaymentStatus.FAILED.name(), null);
                                System.out.println("✗ Payment failed for order: " + orderId + " - " + message);
                        }

                        // MoMo yêu cầu response có format này
                        return ResponseEntity.ok().body("{\"message\": \"success\"}");
                } catch (Exception e) {
                        System.err.println("ERROR processing MoMo IPN: " + e.getMessage());
                        e.printStackTrace();
                        return ResponseEntity.ok().body("{\"message\": \"error\", \"error\": \"" + e.getMessage() + "\"}");
                }
        }

}
