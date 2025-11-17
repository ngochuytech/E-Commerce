package com.example.e_commerce_techshop.controllers.buyer;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.vnpay.PaymentDTO;
import com.example.e_commerce_techshop.dtos.vnpay.PaymentQueryDTO;
import com.example.e_commerce_techshop.dtos.vnpay.PaymentRefundDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.vnpay.IVNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/buyer/payments")
@Tag(name = "Buyer Payment Management", description = "APIs for buyers to manage payment transactions via VNPay gateway")
@SecurityRequirement(name = "bearerAuth")
public class BuyerPaymentController {
    private final IVNPayService vnPayService;

    @PostMapping("/create_payment_url")
    @Operation(
            summary = "Create VNPay payment URL",
            description = "Generate a payment URL for order payment via VNPay gateway. User will be redirected to VNPay website."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment URL created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment details"
            )
    })
    public ResponseEntity<?> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment details including amount, bank code, and language",
                    required = true
            )
            @RequestBody PaymentDTO paymentRequest,
            HttpServletRequest request) throws Exception {
        String paymentUrl = vnPayService.createPaymentUrl(paymentRequest, request);

        return ResponseEntity.ok(ApiResponse.ok(paymentUrl));
    }

    @PostMapping("/query")
    @Operation(
            summary = "Query payment transaction status",
            description = "Query the status of a payment transaction from VNPay. Used to verify if payment was successful."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction status retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid transaction details"
            )
    })
    public ResponseEntity<?> queryTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Query details including order ID and transaction date",
                    required = true
            )
            @RequestBody PaymentQueryDTO paymentQueryDTO,
            HttpServletRequest request) throws Exception {
            String result = vnPayService.queryTransaction(paymentQueryDTO, request);
            return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/refund")
    @Operation(
            summary = "Refund payment transaction",
            description = "Request a refund for a completed payment transaction. Must be initiated by admin or authorized person."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Refund request submitted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid refund details or validation error"
            )
    })
    public ResponseEntity<?> refundTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refund details including order ID, amount, and transaction date",
                    required = true
            )
            @Valid @RequestBody PaymentRefundDTO paymentRefundDTO,
            BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + String.join(", ", errorMessages)));
        }
            String response = vnPayService.refundTransaction(paymentRefundDTO);
            return ResponseEntity.ok().body(ApiResponse.ok(response));
    }
}
