package com.example.e_commerce_techshop.controllers.admin;

import com.example.e_commerce_techshop.models.UserWithdrawalRequest;
import com.example.e_commerce_techshop.responses.admin.AdminWithdrawalResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.WithdrawalRequest;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.UserWithDrawalRequestResponse;
import com.example.e_commerce_techshop.services.userWallet.IUserWalletService;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Quản Lý Rút Tiền", description = "API quản lý yêu cầu rút tiền từ cửa hàng và khách hàng")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWithdrawalController {

        private final IWalletService walletService;
        private final IUserWalletService userWalletService;

        // ============= STORE WITHDRAWALS =============

        @GetMapping("/store")
        @Operation(
                summary = "Lấy danh sách yêu cầu rút tiền từ cửa hàng",
                description = "Admin lấy danh sách các yêu cầu rút tiền từ các chủ cửa hàng",
                tags = {"Quản Lý Rút Tiền"}
        )
        public ResponseEntity<?> getAllStoreWithdrawalRequests(
                        @Parameter(description = "Lọc theo trạng thái: PENDING, REJECTED, COMPLETED", example = "PENDING") @RequestParam(required = false) String status,
                        @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng mục trên một trang", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Trường để sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Hướng sắp xếp (asc: tăng dần, desc: giảm dần)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
                        throws Exception {
                Sort sort = sortDir.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);
                Page<WithdrawalRequest> requests = walletService.getAllWithdrawalRequests(status, pageable);

                Page<AdminWithdrawalResponse> responseList = requests
                                .map(AdminWithdrawalResponse::fromWithdrawalRequest);

                return ResponseEntity.ok(ApiResponse.ok(responseList));
        }

        @PutMapping("/store/{requestId}/reject")
        @Operation(
                summary = "Từ chối yêu cầu rút tiền từ cửa hàng",
                description = "Admin từ chối yêu cầu rút tiền từ chủ cửa hàng",
                tags = {"Quản Lý Rút Tiền"}
        )
        public ResponseEntity<ApiResponse<AdminWithdrawalResponse>> rejectStoreWithdrawalRequest(
                        @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String requestId,
                        @Parameter(description = "Lý do từ chối", example = "Thông tin tài khoản không chính xác") @RequestParam(required = false) String adminNote)
                        throws Exception {
                WithdrawalRequest request = walletService.rejectWithdrawalRequest(requestId, adminNote);
                return ResponseEntity.ok(ApiResponse.ok(AdminWithdrawalResponse.fromWithdrawalRequest(request)));
        }

        @PutMapping("/store/{requestId}/approve")
        @Operation(
                summary = "Phê duyệt yêu cầu rút tiền từ cửa hàng",
                description = "Admin phê duyệt yêu cầu rút tiền từ chủ cửa hàng. Tiền sẽ tự động bị trừ khỏi ví cửa hàng",
                tags = {"Quản Lý Rút Tiền"}
        )
        public ResponseEntity<ApiResponse<AdminWithdrawalResponse>> completeStoreWithdrawalRequest(
                        @Parameter(description = "Withdrawal request ID", example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String requestId,
                        @Parameter(description = "Ghi chú từ admin", example = "Đã chuyển tiền") @RequestParam(required = false) String adminNote)
                        throws Exception {
                WithdrawalRequest request = walletService.completeWithdrawalRequest(requestId, adminNote);
                return ResponseEntity.ok(ApiResponse.ok(AdminWithdrawalResponse.fromWithdrawalRequest(request)));
        }

        // ============= USER/CUSTOMER WITHDRAWALS =============

        @GetMapping("/customer")
        @Operation(
                summary = "Lấy danh sách yêu cầu rút tiền từ khách hàng",
                description = "Admin lấy danh sách các yêu cầu rút tiền từ khách hàng với các trạng thái: PENDING (chờ duyệt), COMPLETED (hoàn thành), REJECTED (bị từ chối)",
                tags = {"Quản Lý Rút Tiền"}
        )
        public ResponseEntity<?> getAllCustomerWithdrawalRequests(
                @Parameter(description = "Lọc theo trạng thái: PENDING, COMPLETED, REJECTED", example = "PENDING") @RequestParam(required = false) String status,
                @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                @Parameter(description = "Số lượng mục trên một trang", example = "10") @RequestParam(defaultValue = "10") int size,
                @Parameter(description = "Trường để sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
                @Parameter(description = "Hướng sắp xếp (asc: tăng dần, desc: giảm dần)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
                throws Exception {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<UserWithdrawalRequest> requests = userWalletService.getAllWithdrawalRequests(status, pageable);

            return ResponseEntity.ok(ApiResponse.ok(requests.map(UserWithDrawalRequestResponse::fromUserWithdrawalRequest)));
        }

        @GetMapping("/customer/{requestId}")
        @Operation(
                summary = "Lấy chi tiết yêu cầu rút tiền từ khách hàng",
                description = "Lấy thông tin chi tiết về một yêu cầu rút tiền cụ thể từ khách hàng",
                tags = {"Quản Lý Rút Tiền"}
        )
        public ResponseEntity<?> getCustomerWithdrawalRequestDetail(
                @Parameter(description = "ID của yêu cầu rút tiền", required = true) @PathVariable String requestId)
                throws Exception {
            UserWithdrawalRequest request = userWalletService.getWithdrawalRequestDetail(null, requestId);
            return ResponseEntity.ok(ApiResponse.ok(UserWithDrawalRequestResponse.fromUserWithdrawalRequest(request)));
        }

        @PutMapping("/customer/{requestId}/approve")
        @Operation(
                summary = "Phê duyệt yêu cầu rút tiền từ khách hàng",
                description = "Admin phê duyệt yêu cầu rút tiền từ khách hàng. Tiền sẽ tự động bị trừ khỏi ví khách hàng",
                tags = {"Quản Lý Rút Tiền"}
        )
        public ResponseEntity<?> approveCustomerWithdrawalRequest(
                @Parameter(description = "ID của yêu cầu rút tiền", required = true) @PathVariable String requestId,
                @Parameter(description = "Ghi chú từ admin", example = "Đã chuyển tiền vào tài khoản ngân hàng") @RequestParam(required = false) String adminNote)
                throws Exception {
            UserWithdrawalRequest request = userWalletService.completeWithdrawalRequest(requestId, adminNote);
            return ResponseEntity.ok(ApiResponse.ok(UserWithDrawalRequestResponse.fromUserWithdrawalRequest(request)));
        }

        @PutMapping("/customer/{requestId}/reject")
        @Operation(
                summary = "Từ chối yêu cầu rút tiền từ khách hàng",
                description = "Admin từ chối yêu cầu rút tiền từ khách hàng. Tiền vẫn được giữ lại trong ví",
                tags = {"Quản Lý Rút Tiền"}
        )
        public ResponseEntity<?> rejectCustomerWithdrawalRequest(
                @Parameter(description = "ID của yêu cầu rút tiền", required = true) @PathVariable String requestId,
                @Parameter(description = "Lý do từ chối", example = "Thông tin tài khoản ngân hàng không chính xác") @RequestParam(required = false) String adminNote)
                throws Exception {
            UserWithdrawalRequest request = userWalletService.rejectWithdrawalRequest(requestId, adminNote);
            return ResponseEntity.ok(ApiResponse.ok(UserWithDrawalRequestResponse.fromUserWithdrawalRequest(request)));
        }
}
