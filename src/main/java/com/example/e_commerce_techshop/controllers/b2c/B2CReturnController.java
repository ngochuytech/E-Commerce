package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.b2c.ReturnQualityDisputeDTO;
import com.example.e_commerce_techshop.dtos.b2c.ReturnResponseDTO;
import com.example.e_commerce_techshop.dtos.buyer.DisputeRequestDTO;
import com.example.e_commerce_techshop.models.Dispute;
import com.example.e_commerce_techshop.models.ReturnRequest;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.admin.DisputeResponse;
import com.example.e_commerce_techshop.responses.buyer.ReturnRequestResponse;
import com.example.e_commerce_techshop.services.returnrequest.IReturnRequestService;
import com.example.e_commerce_techshop.services.store.IStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/returns")
@RequiredArgsConstructor
@Tag(name = "B2C Return Management", description = "APIs for store to manage return requests")
@SecurityRequirement(name = "bearerAuth")
public class B2CReturnController {

    private final IReturnRequestService returnRequestService;
    private final IStoreService storeService;

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Danh sách yêu cầu trả hàng", description = "Lấy danh sách yêu cầu trả hàng của store")
    public ResponseEntity<?> getReturnRequests(
            @Parameter(description = "Filter by status (PENDING, APPROVED, REJECTED, DISPUTED, RETURNING, RETURNED, REFUNDED, CLOSED)") @RequestParam(required = false) String status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @PathVariable String storeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReturnRequest> returnRequests = returnRequestService.getStoreReturnRequests(store.getId(), status,
                pageable);
        // Map và thêm disputes cho từng return request
        Page<ReturnRequestResponse> responsePage = returnRequests.map(returnRequest -> {
            try {
                List<Dispute> disputes = returnRequestService.getDisputesByReturnRequest(returnRequest.getId());
                return ReturnRequestResponse.fromReturnRequestWithDisputes(returnRequest, disputes);
            } catch (Exception e) {
                // Nếu có lỗi khi lấy disputes, vẫn trả về return request nhưng không có disputes
                return ReturnRequestResponse.fromReturnRequest(returnRequest);
            }
        });
        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }

    @GetMapping("/store/{storeId}/returnRequest/{returnRequestId}")
    @Operation(summary = "Chi tiết yêu cầu trả hàng", description = "Xem chi tiết yêu cầu trả hàng (bao gồm disputes liên quan)")
    public ResponseEntity<?> getReturnRequestDetail(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @PathVariable String storeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());
        ReturnRequest returnRequest = returnRequestService.getStoreReturnRequestDetail(store.getId(), returnRequestId);
        List<Dispute> disputes = returnRequestService.getDisputesByReturnRequest(returnRequestId);

        return ResponseEntity.ok(ApiResponse.ok(
                ReturnRequestResponse.fromReturnRequestWithDisputes(returnRequest, disputes)));
    }

    @GetMapping("/store/{storeId}/count-by-status")
    @Operation(summary = "Thống kê yêu cầu trả hàng theo trạng thái", description = "Đếm số lượng yêu cầu trả hàng theo từng trạng thái")
    public ResponseEntity<?> countReturnRequestsByStatus(
            @PathVariable String storeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());

        // Đếm số lượng theo trạng thái
        Map<String, Long> countByStatus = returnRequestService.countStoreReturnRequestsByStatus(store.getId());
        return ResponseEntity.ok(ApiResponse.ok(countByStatus));
    }

    @PutMapping(value = "/store/{storeId}/returnRequest/{returnRequestId}/respond", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Phản hồi yêu cầu trả hàng", description = "Store chấp nhận hoặc từ chối yêu cầu trả hàng")
    public ResponseEntity<?> respondToReturnRequest(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @PathVariable String storeId,
            @Valid @RequestPart("dto") ReturnResponseDTO dto,
            @Parameter(description = "Ảnh/video minh chứng (tối đa 5 file)") @RequestParam(value = "evidenceFiles", required = false) List<MultipartFile> evidenceFiles,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());
        ReturnRequest returnRequest = returnRequestService.respondToReturnRequest(store.getId(), returnRequestId, dto,
                evidenceFiles);
        return ResponseEntity.ok(ApiResponse.ok(ReturnRequestResponse.fromReturnRequest(returnRequest)));
    }

    @PutMapping("/store/{storeId}/returnRequest/{returnRequestId}/confirm-ok")
    @Operation(summary = "Xác nhận hàng trả về OK", description = "Store xác nhận hàng trả về đạt yêu cầu và đồng ý hoàn tiền cho buyer")
    public ResponseEntity<?> confirmReturnedGoodsOk(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @PathVariable String storeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());
        ReturnRequest returnRequest = returnRequestService.confirmReturnedGoodsOk(store.getId(), returnRequestId);
        return ResponseEntity.ok(ApiResponse.ok(ReturnRequestResponse.fromReturnRequest(returnRequest)));
    }

    @PostMapping(value = "/store/{storeId}/returnRequest/{returnRequestId}/dispute-quality", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Khiếu nại hàng trả về có vấn đề", description = "Store khiếu nại khi hàng trả về bị hư hỏng/không đúng. Upload ảnh/video minh chứng. Admin sẽ xem xét và quyết định.")
    public ResponseEntity<?> createReturnQualityDispute(
            @Parameter(description = "Return Request ID") @PathVariable String returnRequestId,
            @PathVariable String storeId,
            @Parameter(description = "Lý do khiếu nại") @RequestParam String reason,
            @Parameter(description = "Mô tả chi tiết") @RequestParam(required = false) String description,
            @Parameter(description = "Ảnh/video minh chứng (tối đa 5 file)") @RequestParam(value = "evidenceFiles", required = false) List<MultipartFile> evidenceFiles,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());

        ReturnQualityDisputeDTO dto = ReturnQualityDisputeDTO.builder()
                .reason(reason)
                .description(description)
                .build();

        Dispute dispute = returnRequestService.createReturnQualityDispute(store.getId(), returnRequestId, dto,
                evidenceFiles);
        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @GetMapping("/store/{storeId}/disputes")
    @Operation(summary = "Danh sách khiếu nại", description = "Lấy danh sách khiếu nại liên quan đến store")
    public ResponseEntity<?> getDisputes(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @PathVariable String storeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Dispute> disputes = returnRequestService.getStoreDisputes(store.getId(), pageable);
        Page<DisputeResponse> responsePage = disputes.map(DisputeResponse::fromDispute);
        return ResponseEntity.ok(ApiResponse.ok(responsePage));
    }

    @GetMapping("/store/{storeId}/disputes/{disputeId}")
    @Operation(summary = "Chi tiết khiếu nại", description = "Xem chi tiết một khiếu nại cụ thể")
    public ResponseEntity<?> getDisputeDetail(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @PathVariable String storeId,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());
        Dispute dispute = returnRequestService.getDisputeDetail(disputeId);

        // Kiểm tra dispute có thuộc về store này không
        if (!dispute.getStore().getId().equals(store.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem khiếu nại này");
        }

        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }

    @PostMapping(value = "/store/{storeId}/disputes/{disputeId}/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Thêm tin nhắn vào khiếu nại", description = "Store thêm tin nhắn/bằng chứng vào khiếu nại. Upload ảnh/video đính kèm.")
    public ResponseEntity<?> addDisputeMessage(
            @Parameter(description = "Dispute ID") @PathVariable String disputeId,
            @PathVariable String storeId,
            @Parameter(description = "Nội dung tin nhắn") @RequestParam String content,
            @Parameter(description = "Ảnh/video đính kèm (tối đa 5 file)") @RequestParam(value = "attachmentFiles", required = false) List<MultipartFile> attachmentFiles,
            @AuthenticationPrincipal User currentUser) throws Exception {

        Store store = storeService.getStoreByIdAndOwnerId(storeId, currentUser.getId());

        DisputeRequestDTO dto = DisputeRequestDTO.builder()
                .content(content)
                .build();

        Dispute dispute = returnRequestService.addStoreDisputeMessage(store.getId(), disputeId, dto, attachmentFiles);
        return ResponseEntity.ok(ApiResponse.ok(DisputeResponse.fromDispute(dispute)));
    }
}
