package com.example.e_commerce_techshop.controllers.buyer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.models.UserTransaction;
import com.example.e_commerce_techshop.models.UserWallet;
import com.example.e_commerce_techshop.models.UserWithdrawalRequest;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.UserWithDrawalRequestResponse;
import com.example.e_commerce_techshop.responses.buyer.BuyerTransactionResponse;
import com.example.e_commerce_techshop.responses.buyer.BuyerWalletResponse;
import com.example.e_commerce_techshop.services.userWallet.IUserWalletService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/buyer/wallet")
@RequiredArgsConstructor
@Tag(name = "Ví Điện Tử Khách Hàng", description = "API quản lý ví điện tử cho khách hàng - hoàn tiền, rút tiền, xem lịch sử giao dịch")
@SecurityRequirement(name = "bearer-jwt")
public class BuyerWalletController {

    private final IUserWalletService userWalletService;

    /**
     * Lấy thông tin ví của khách hàng
     */
    @GetMapping("/info")
    @Operation(
            summary = "Lấy thông tin ví",
            description = "Lấy thông tin chi tiết về ví điện tử của khách hàng bao gồm: số dư, tổng tiền hoàn lại, tổng tiền đã chi tiêu",
            tags = {"Ví Điện Tử Khách Hàng"}
    )
    public ResponseEntity<?> getWalletInfo(@AuthenticationPrincipal User user) throws Exception {
        UserWallet wallet = userWalletService.getWalletInfo(user.getId());

        return ResponseEntity.ok(ApiResponse.ok(BuyerWalletResponse.fromUserWallet(wallet)));
    }

    /**
     * Lấy số dư ví
     */
    @GetMapping("/balance")
    @Operation(
            summary = "Lấy số dư ví",
            description = "Lấy số dư hiện tại trong ví điện tử",
            tags = {"Ví Điện Tử Khách Hàng"}
    )
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal User user) throws Exception {
        BigDecimal balance = userWalletService.getWalletBalance(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("balance", balance);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * Lấy lịch sử giao dịch
     */
    @GetMapping("/transactions")
    @Operation(
            summary = "Lấy lịch sử giao dịch",
            description = "Lấy danh sách lịch sử giao dịch của khách hàng (hoàn tiền, thanh toán bằng ví, rút tiền, điều chỉnh số dư)",
            tags = {"Ví Điện Tử Khách Hàng"}
    )
    public ResponseEntity<?> getTransactionHistory(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên một trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường để sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc: tăng dần, desc: giảm dần)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserTransaction> transactions = userWalletService.getTransactionHistory(user.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.ok(transactions.map(BuyerTransactionResponse::fromTransaction)));

    }

    /**
     * Tạo yêu cầu rút tiền
     */
    @PostMapping("/withdrawal-request")
    @Operation(
            summary = "Tạo yêu cầu rút tiền",
            description = "Khách hàng tạo yêu cầu rút tiền từ ví về tài khoản ngân hàng. Yêu cầu sẽ chờ admin duyệt",
            tags = {"Ví Điện Tử Khách Hàng"}
    )
    public ResponseEntity<?> createWithdrawalRequest(
            @AuthenticationPrincipal User user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin yêu cầu rút tiền",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WithdrawalRequestDTO.class)
                    )
            )
            @RequestBody WithdrawalRequestDTO dto) throws Exception {
        UserWithdrawalRequest request = userWalletService.createWithdrawalRequest(
                user.getId(),
                dto.getAmount(),
                dto.getBankName(),
                dto.getBankAccountNumber(),
                dto.getBankAccountName(),
                dto.getNote());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(UserWithDrawalRequestResponse.fromUserWithdrawalRequest(request)));
    }

    /**
     * Lấy danh sách yêu cầu rút tiền của khách hàng
     */
    @GetMapping("/withdrawal-requests")
    @Operation(
            summary = "Lấy danh sách yêu cầu rút tiền",
            description = "Lấy danh sách các yêu cầu rút tiền của khách hàng (PENDING: chờ duyệt, APPROVED: đã duyệt, REJECTED: bị từ chối, COMPLETED: hoàn thành)",
            tags = {"Ví Điện Tử Khách Hàng"}
    )
    public ResponseEntity<?> getWithdrawalRequests(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên một trang", example = "10") @RequestParam(defaultValue = "10") int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserWithdrawalRequest> requests = userWalletService.getWithdrawalRequests(user.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.ok(requests.map(UserWithDrawalRequestResponse::fromUserWithdrawalRequest)));
    }

    /**
     * Lấy chi tiết yêu cầu rút tiền
     */
    @GetMapping("/withdrawal-requests/{requestId}")
    @Operation(
            summary = "Lấy chi tiết yêu cầu rút tiền",
            description = "Lấy thông tin chi tiết về một yêu cầu rút tiền cụ thể",
            tags = {"Ví Điện Tử Khách Hàng"}
    )
    public ResponseEntity<?> getWithdrawalRequestDetail(
            @AuthenticationPrincipal User user,
            @Parameter(description = "ID của yêu cầu rút tiền", required = true) @PathVariable String requestId) throws Exception {
        UserWithdrawalRequest request = userWalletService.getWithdrawalRequestDetail(user.getId(), requestId);

        return ResponseEntity.ok(ApiResponse.ok(UserWithDrawalRequestResponse.fromUserWithdrawalRequest(request)));
    }
}
