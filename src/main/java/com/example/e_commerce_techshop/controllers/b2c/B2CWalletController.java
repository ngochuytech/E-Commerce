package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.buyer.WithdrawalRequestDTO;
import com.example.e_commerce_techshop.models.Transaction;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.models.Wallet;
import com.example.e_commerce_techshop.models.WithdrawalRequest;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.wallet.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${api.prefix}/b2c/wallet")
@RequiredArgsConstructor
@Tag(name = "B2C Wallet Management", description = "APIs for store owners to manage wallet balance and withdrawal requests")
@SecurityRequirement(name = "bearerAuth")
public class B2CWalletController {

    private final IWalletService walletService;

    /**
     * Lấy thông tin ví của store
     */
    @GetMapping("/store/{storeId}")
    @Operation(
            summary = "Get store wallet information",
            description = "Retrieve wallet balance and details for a specific store"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Wallet information retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Store not found or unauthorized access"
            )
    })
    public ResponseEntity<ApiResponse<Wallet>> getStoreWallet(
            @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String storeId,
            @AuthenticationPrincipal User user) {
        try {
            Wallet wallet = walletService.getStoreWallet(storeId);
            return ResponseEntity.ok(ApiResponse.ok(wallet));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử giao dịch
     */
    @GetMapping("/store/{storeId}/transactions")
    @Operation(
            summary = "Get transaction history",
            description = "Retrieve paginated transaction history for a store including deposits, withdrawals, and commissions"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction history retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Store not found or unauthorized access"
            )
    })
    public ResponseEntity<ApiResponse<Page<Transaction>>> getTransactionHistory(
            @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String storeId,
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        try {
            Page<Transaction> transactions = walletService.getTransactionHistory(storeId, page, size);
            return ResponseEntity.ok(ApiResponse.ok(transactions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Tạo yêu cầu rút tiền
     */
    @PostMapping("/store/{storeId}/withdrawal")
    @Operation(
            summary = "Create withdrawal request",
            description = "Create a new withdrawal request to transfer store balance to bank account. Must be approved by admin."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal request created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or insufficient balance"
            )
    })
    public ResponseEntity<ApiResponse<WithdrawalRequest>> createWithdrawalRequest(
            @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String storeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Withdrawal request details including amount and bank information",
                    required = true
            )
            @RequestBody @Valid WithdrawalRequestDTO dto,
            @AuthenticationPrincipal User user) {
        try {
            WithdrawalRequest request = walletService.createWithdrawalRequest(
                    storeId,
                    dto.getAmount(),
                    dto.getBankName(),
                    dto.getBankAccountNumber(),
                    dto.getBankAccountName(),
                    dto.getNote()
            );
            return ResponseEntity.ok(ApiResponse.ok(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách yêu cầu rút tiền của store
     */
    @GetMapping("/store/{storeId}/withdrawals")
    @Operation(
            summary = "Get withdrawal requests for store",
            description = "Retrieve paginated list of all withdrawal requests for a specific store"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal requests retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Store not found or unauthorized access"
            )
    })
    public ResponseEntity<ApiResponse<Page<WithdrawalRequest>>> getWithdrawalRequests(
            @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String storeId,
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        try {
            Page<WithdrawalRequest> requests = walletService.getWithdrawalRequests(storeId, page, size);
            return ResponseEntity.ok(ApiResponse.ok(requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết yêu cầu rút tiền
     */
    @GetMapping("/store/{storeId}/withdrawal/{requestId}")
    @Operation(
            summary = "Get withdrawal request details",
            description = "Retrieve detailed information about a specific withdrawal request"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal request details retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Store or request not found or unauthorized access"
            )
    })
    public ResponseEntity<ApiResponse<WithdrawalRequest>> getWithdrawalRequestDetail(
            @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String storeId,
            @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d2")
            @PathVariable String requestId,
            @AuthenticationPrincipal User user) {
        try {
            WithdrawalRequest request = walletService.getWithdrawalRequestDetail(storeId, requestId);
            return ResponseEntity.ok(ApiResponse.ok(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
