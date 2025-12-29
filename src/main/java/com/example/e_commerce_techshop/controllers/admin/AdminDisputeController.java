package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.admin.DisputeDecisionDTO;
import com.example.e_commerce_techshop.dtos.admin.ReturnQualityDecisionDTO;
import com.example.e_commerce_techshop.dtos.buyer.DisputeRequestDTO;
import com.example.e_commerce_techshop.models.Dispute;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.admin.DisputeResponse;
import com.example.e_commerce_techshop.responses.buyer.ReturnRequestResponse;
import com.example.e_commerce_techshop.services.returnrequest.IReturnRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/disputes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dispute Management", description = "APIs for admin to manage disputes")
@SecurityRequirement(name = "bearerAuth")
public class AdminDisputeController {

    private final IReturnRequestService returnRequestService;

    @GetMapping
    @Operation(summary = "Danh sách khiếu nại", description = "Lấy danh sách tất cả khiếu nại")
    public ResponseEntity<?> getAllDisputes(
            @Parameter(description = "Filter by status (OPEN, IN_PROGRESS, RESOLVED)") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by dispute type (RETURN_REJECTION, RETURN_QUALITY)") @RequestParam(required = false) String disputeType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User admin) throws Exception {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Dispute> disputes = returnRequestService.getAllDisputes(status, disputeType, pageable);
        Page<DisputeResponse> responsePage = disputes.map(DisputeResponse::fromDispute);
        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }

    @GetMapping("/{disputeId}")
    @Operation(summary = "Chi tiết khiếu nại", description = "Xem chi tiết khiếu nại")
    public ResponseEntity<?> getDisputeDetail(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @AuthenticationPrincipal User admin) throws Exception {

        Dispute dispute = returnRequestService.getDisputeDetail(disputeId);
        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @PostMapping("/{disputeId}/message")
    @Operation(summary = "Thêm tin nhắn vào khiếu nại", description = "Admin thêm tin nhắn/phản hồi vào khiếu nại")
    public ResponseEntity<?> addDisputeMessage(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @Valid @RequestBody DisputeRequestDTO dto,
            @AuthenticationPrincipal User admin) throws Exception {

        Dispute dispute = returnRequestService.addAdminDisputeMessage(admin, disputeId, dto);
        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @PutMapping("/{disputeId}/resolve")
    @Operation(summary = "Giải quyết khiếu nại từ chối đơn trả hàng", description = "Admin quyết định khiếu nại khi buyer khiếu nại việc store từ chối đơn trả hàng. APPROVE_RETURN = đồng ý với buyer (chấp nhận trả hàng), REJECT_RETURN = đồng ý với store (giữ nguyên từ chối)")
    public ResponseEntity<?> resolveDispute(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @Valid @RequestBody DisputeDecisionDTO dto,
            @AuthenticationPrincipal User admin) throws Exception {

        Dispute dispute = returnRequestService.resolveDispute(admin, disputeId, dto);
        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @PutMapping("/{disputeId}/resolve-quality")
    @Operation(summary = "Giải quyết khiếu nại chất lượng hàng trả về", description = "Admin quyết định khi store khiếu nại hàng trả về có vấn đề."
            +
            "APPROVE_STORE = đồng ý với store (hoàn tiền cho store), REJECT_STORE = đồng ý với buyer (hoàn tiền cho buyer), PARTIAL_REFUND = hoàn tiền một phần cho buyer. Lưu ý số tiền hoàn lại (1 phần) cho buyer phải nhỏ hơn số tiền shop được nhận.")
    public ResponseEntity<?> resolveReturnQualityDispute(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @Valid @RequestBody ReturnQualityDecisionDTO dto,
            @AuthenticationPrincipal User admin) throws Exception {

        ReturnRequest returnRequest = returnRequestService.resolveReturnQualityDispute(admin, disputeId, dto);
        return ResponseEntity.ok(ApiResponse.ok(ReturnRequestResponse.fromReturnRequest(returnRequest)));
    }

}
