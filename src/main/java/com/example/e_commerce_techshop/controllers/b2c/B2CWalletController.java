package com.example.e_commerce_techshop.controllers.b2c;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.WithdrawalRequestDTO;
import com.example.e_commerce_techshop.models.Transaction;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.models.Wallet;
import com.example.e_commerce_techshop.models.WithdrawalRequest;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.TransactionResponse;
import com.example.e_commerce_techshop.responses.WalletResponse;
import com.example.e_commerce_techshop.responses.admin.AdminWithdrawalResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/wallet")
@RequiredArgsConstructor
@Tag(name = "B2C Wallet Management", description = "APIs for store owners to manage wallet balance and withdrawal requests")
@SecurityRequirement(name = "bearerAuth")
public class B2CWalletController {

        private final IWalletService walletService;
        private final IStoreService storeService;

        /**
         * Lấy thông tin ví của store
         */
        @GetMapping("/store/{storeId}")
        @Operation(summary = "Get store wallet information", description = "Retrieve wallet balance and details for a specific store")
        public ResponseEntity<ApiResponse<WalletResponse>> getStoreWallet(
                        @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
                        @AuthenticationPrincipal User user) throws Exception {
                Wallet wallet = walletService.getStoreWallet(storeId);
                return ResponseEntity.ok(ApiResponse.ok(WalletResponse.fromWallet(wallet)));
        }

        /**
         * Lấy lịch sử giao dịch
         */
        @GetMapping("/store/{storeId}/transactions")
        @Operation(summary = "Get transaction history", description = "Retrieve paginated transaction history for a store including deposits, withdrawals, and commissions")
        public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionHistory(
                        @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir,
                        @AuthenticationPrincipal User user) throws Exception {
                Sort sort = sortDir.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();

                Pageable pageable = PageRequest.of(page, size, sort);
                Page<Transaction> transactions = walletService.getTransactionHistory(storeId, pageable);
                return ResponseEntity.ok(ApiResponse.ok(transactions.map(TransactionResponse::fromTransaction)));
        }

        /**
         * Tạo yêu cầu rút tiền
         */
        @PostMapping("/store/{storeId}/withdrawal")
        @Operation(summary = "Create withdrawal request", description = "Create a new withdrawal request to transfer store balance to bank account. Must be approved by admin.")
        public ResponseEntity<?> createWithdrawalRequest(
                        @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Withdrawal request details including amount and bank information", required = true) @RequestBody @Valid WithdrawalRequestDTO dto,
                        @AuthenticationPrincipal User user) throws Exception {
                // Kiểm tra shop có bị banned không
                storeService.validateStoreNotBanned(storeId);
                
                WithdrawalRequest request = walletService.createWithdrawalRequest(
                                storeId,
                                dto.getAmount(),
                                dto.getBankName(),
                                dto.getBankAccountNumber(),
                                dto.getBankAccountName(),
                                dto.getNote());
                return ResponseEntity.ok(ApiResponse.ok(AdminWithdrawalResponse.fromWithdrawalRequest(request)));
        }

        /**
         * Lấy danh sách yêu cầu rút tiền của store
         */
        @GetMapping("/store/{storeId}/withdrawals")
        @Operation(summary = "Get withdrawal requests for store", description = "Retrieve paginated list of all withdrawal requests for a specific store")
        public ResponseEntity<?> getWithdrawalRequests(
                        @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir,
                        @AuthenticationPrincipal User user) throws Exception {
                Sort sort = sortDir.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<WithdrawalRequest> requests = walletService.getWithdrawalRequests(storeId, pageable);
                Page<AdminWithdrawalResponse> responseList = requests.map(AdminWithdrawalResponse::fromWithdrawalRequest);
                return ResponseEntity.ok(ApiResponse.ok(responseList));
        }

        /**
         * Lấy chi tiết yêu cầu rút tiền
         */
        @GetMapping("/store/{storeId}/withdrawal/{requestId}")
        @Operation(summary = "Get withdrawal request details", description = "Retrieve detailed information about a specific withdrawal request")
        public ResponseEntity<?> getWithdrawalRequestDetail(
                        @Parameter(description = "Store ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
                        @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable String requestId,
                        @AuthenticationPrincipal User user) throws Exception {
                WithdrawalRequest request = walletService.getWithdrawalRequestDetail(storeId, requestId);
                return ResponseEntity.ok(ApiResponse.ok(AdminWithdrawalResponse.fromWithdrawalRequest(request)));
        }
}
