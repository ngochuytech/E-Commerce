package com.example.e_commerce_techshop.controllers.admin;

import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.admin.ProcessRefundRequestDTO;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.admin.RefundRequestResponse;
import com.example.e_commerce_techshop.services.refund.IRefundAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/refunds")
@RequiredArgsConstructor
@Tag(name = "Admin Refund Management", description = "APIs for admin to manage refund requests")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundController {
    
    private final IRefundAdminService refundAdminService;

    @GetMapping
    @Operation(summary = "Lấy danh sách yêu cầu hoàn tiền", 
               description = "Lấy danh sách tất cả yêu cầu hoàn tiền với filter theo trạng thái và phân trang")
    public ResponseEntity<?> getRefundRequests(
            @Parameter(description = "Trạng thái filter (PENDING, COMPLETED, REJECTED). Để trống để lấy tất cả") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") 
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<RefundRequestResponse> refundRequests = refundAdminService.getRefundRequests(status, pageable);
        
        return ResponseEntity.ok(ApiResponse.ok(refundRequests));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết yêu cầu hoàn tiền", 
               description = "Lấy thông tin chi tiết của một yêu cầu hoàn tiền bao gồm thông tin khách hàng và tài khoản ngân hàng")
    public ResponseEntity<?> getRefundRequestDetail(
            @Parameter(description = "ID của yêu cầu hoàn tiền") 
            @PathVariable String id) throws Exception {
        RefundRequestResponse refundRequest = refundAdminService.getRefundRequestDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(refundRequest));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Thống kê yêu cầu hoàn tiền", 
               description = "Lấy số lượng yêu cầu hoàn tiền theo từng trạng thái")
    public ResponseEntity<?> getRefundStatistics() {
        Map<String, Object> statistics = refundAdminService.getRefundStatistics();
        return ResponseEntity.ok(ApiResponse.ok(statistics));
    }

    @PostMapping("/process")
    @Operation(summary = "Xử lý yêu cầu hoàn tiền", 
               description = "Duyệt (APPROVE) hoặc Từ chối (REJECT) yêu cầu hoàn tiền. " +
                           "Nếu APPROVE, cần nhập refundTransactionId. Nếu REJECT, cần nhập rejectionReason.")
    public ResponseEntity<?> processRefundRequest(
            @RequestBody ProcessRefundRequestDTO dto,
            @AuthenticationPrincipal User admin) throws Exception {
        
        RefundRequestResponse result = refundAdminService.processRefundRequest(dto, admin);
        
        String message = "APPROVE".equalsIgnoreCase(dto.getAction()) 
                ? "Đã duyệt yêu cầu hoàn tiền thành công" 
                : "Đã từ chối yêu cầu hoàn tiền";
        
        return ResponseEntity.ok(ApiResponse.ok(message));
    }
}
