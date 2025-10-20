package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/stores")
@RequiredArgsConstructor
@Tag(name = "Admin Store Management", description = "APIs for admin to manage stores - approval, rejection, status updates")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminStoreController {
    private final IStoreService storeService;

    @GetMapping("/pending")
    @Operation(summary = "Get pending stores", description = "Retrieve all stores that are waiting for admin approval")
    public ResponseEntity<?> getPendingStores(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StoreResponse> stores = storeService.getPendingStores(pageable);
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @GetMapping("/approved")
    @Operation(summary = "Get approved stores", description = "Retrieve all stores that have been approved by admin")
    public ResponseEntity<?> getApprovedStores(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StoreResponse> stores = storeService.getApprovedStores(pageable);
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @PutMapping("/{storeId}/approve")
    @Operation(summary = "Approve store", description = "Approve a pending store registration")
    public ResponseEntity<?> approveStore(
            @Parameter(description = "ID of the store to approve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        storeService.approveStore(storeId);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}/reject")
    @Operation(summary = "Reject store", description = "Reject a pending store registration with a reason")
    public ResponseEntity<?> rejectStore(
            @Parameter(description = "ID of the store to reject", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Reason for rejection", required = true, example = "Không đủ điều kiện kinh doanh") @RequestParam String reason)
            throws Exception {
        storeService.rejectStore(storeId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}/status")
    @Operation(summary = "Update store status", description = "Update the status of a store (activate/deactivate)")
    public ResponseEntity<?> updateStoreStatus(
            @Parameter(description = "ID of the store to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "New status for the store", required = true, example = "ACTIVE") @RequestParam String status)
            throws Exception {
        if (!Store.isValidStatus(status)) {
            String validStatuses = String.join(", ", Store.getValidStatuses());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Status không hợp lệ: '" + status + "'. Chỉ chấp nhận: " + validStatuses));
        }

        storeService.updateStoreStatus(storeId, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái cửa hàng thành công!"));
    }

    @DeleteMapping("/{storeId}")
    @Operation(summary = "Soft delete store", description = "Soft delete a store by marking it as deleted")
    public ResponseEntity<?> softDeleteStore(
            @Parameter(description = "ID of the store to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        storeService.updateStoreStatus(storeId, "DELETED");
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa (mềm) cửa hàng"));
    }
}
