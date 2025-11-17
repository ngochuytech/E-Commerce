package com.example.e_commerce_techshop.controllers.admin;

import com.example.e_commerce_techshop.models.WithdrawalRequest;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.wallet.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/admin/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Admin Withdrawal Management", description = "Admin APIs for managing withdrawal requests from store owners")
@SecurityRequirement(name = "bearerAuth")
public class AdminWithdrawalController {

    private final IWalletService walletService;

    /**
     * Lấy tất cả yêu cầu rút tiền
     */
    @GetMapping("")
    @Operation(
            summary = "Get all withdrawal requests",
            description = "Retrieve paginated list of all withdrawal requests with optional status filter. Admin only."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved withdrawal requests",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid parameters"
            )
    })
    public ResponseEntity<ApiResponse<Page<WithdrawalRequest>>> getAllWithdrawalRequests(
            @Parameter(description = "Filter by status: PENDING, APPROVED, REJECTED, COMPLETED", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<WithdrawalRequest> requests = walletService.getAllWithdrawalRequests(status, page, size);
            return ResponseEntity.ok(ApiResponse.ok(requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Duyệt yêu cầu rút tiền
     */
    @PutMapping("/{requestId}/approve")
    @Operation(
            summary = "Approve withdrawal request",
            description = "Admin approves a pending withdrawal request and can add a note"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal request approved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or request not found"
            )
    })
    public ResponseEntity<ApiResponse<WithdrawalRequest>> approveWithdrawalRequest(
            @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String requestId,
            @Parameter(description = "Optional admin note for approval", example = "Approved for processing")
            @RequestParam(required = false) String adminNote) {
        try {
            WithdrawalRequest request = walletService.approveWithdrawalRequest(requestId, adminNote);
            return ResponseEntity.ok(ApiResponse.ok(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Từ chối yêu cầu rút tiền
     */
    @PutMapping("/{requestId}/reject")
    @Operation(
            summary = "Reject withdrawal request",
            description = "Admin rejects a pending withdrawal request and can provide a rejection reason"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal request rejected successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or request not found"
            )
    })
    public ResponseEntity<ApiResponse<WithdrawalRequest>> rejectWithdrawalRequest(
            @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String requestId,
            @Parameter(description = "Reason for rejection", example = "Invalid bank account information")
            @RequestParam(required = false) String adminNote) {
        try {
            WithdrawalRequest request = walletService.rejectWithdrawalRequest(requestId, adminNote);
            return ResponseEntity.ok(ApiResponse.ok(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Hoàn thành chuyển tiền (sau khi đã chuyển tiền thực tế)
     */
    @PutMapping("/{requestId}/complete")
    @Operation(
            summary = "Complete withdrawal transfer",
            description = "Mark withdrawal request as completed after money has been transferred"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal transfer completed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or request not found"
            )
    })
    public ResponseEntity<ApiResponse<WithdrawalRequest>> completeWithdrawalRequest(
            @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d1")
            @PathVariable String requestId) {
        try {
            WithdrawalRequest request = walletService.completeWithdrawalRequest(requestId);
            return ResponseEntity.ok(ApiResponse.ok(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
