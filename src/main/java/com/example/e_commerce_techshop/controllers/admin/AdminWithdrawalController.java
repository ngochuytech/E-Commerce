package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.WithdrawalRequest;
import com.example.e_commerce_techshop.responses.AdminWithdrawalResponse;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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
        @Operation(summary = "Get all withdrawal requests", description = "Retrieve paginated list of all withdrawal requests with optional status filter. Admin only.")
        public ResponseEntity<ApiResponse<Page<AdminWithdrawalResponse>>> getAllWithdrawalRequests(
                        @Parameter(description = "Filter by status: PENDING, REJECTED, COMPLETED", example = "PENDING") @RequestParam(required = false) String status,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
                        throws Exception {
                Sort sort = sortDir.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<WithdrawalRequest> requests = walletService.getAllWithdrawalRequests(status, pageable);
                
                // Convert to AdminWithdrawalResponse
                Page<AdminWithdrawalResponse> responseList = requests.map(AdminWithdrawalResponse::fromWithdrawalRequest);
                
                return ResponseEntity.ok(ApiResponse.ok(responseList));
        }

        /**
         * Từ chối yêu cầu rút tiền
         */
        @PutMapping("/{requestId}/reject")
        @Operation(summary = "Reject withdrawal request", description = "Admin rejects a pending withdrawal request and provides rejection reason")
        public ResponseEntity<ApiResponse<AdminWithdrawalResponse>> rejectWithdrawalRequest(
                        @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String requestId,
                        @Parameter(description = "Rejection reason", example = "Invalid bank account information") @RequestParam(required = false) String adminNote)
                        throws Exception {
                WithdrawalRequest request = walletService.rejectWithdrawalRequest(requestId, adminNote);
                return ResponseEntity.ok(ApiResponse.ok(AdminWithdrawalResponse.fromWithdrawalRequest(request)));
        }

        /**
         * Hoàn thành chuyển tiền (sau khi đã chuyển tiền thực tế)
         */
        @PutMapping("/{requestId}/complete")
        @Operation(summary = "Complete withdrawal transfer", description = "Mark withdrawal request as completed after money has been transferred. Auto-deducts from wallet.")
        public ResponseEntity<ApiResponse<AdminWithdrawalResponse>> completeWithdrawalRequest(
                        @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String requestId,
                        @Parameter(description = "Optional admin note", example = "Transferred successfully") @RequestParam(required = false) String adminNote)
                        throws Exception {
                WithdrawalRequest request = walletService.completeWithdrawalRequest(requestId, adminNote);
                return ResponseEntity.ok(ApiResponse.ok(AdminWithdrawalResponse.fromWithdrawalRequest(request)));
        }
}
