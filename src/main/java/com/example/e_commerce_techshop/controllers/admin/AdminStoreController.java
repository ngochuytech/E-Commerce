package com.example.e_commerce_techshop.controllers.admin;

import java.util.List;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {
    private final IStoreService storeService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingStores() {
        List<StoreResponse> stores = storeService.getPendingStores();
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @GetMapping("/approved")
    public ResponseEntity<?> getApprovedStores() {
        List<StoreResponse> stores = storeService.getApprovedStores();
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @PutMapping("/{storeId}/approve")
    public ResponseEntity<?> approveStore(@PathVariable String storeId) {
        try {
            storeService.approveStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Duyệt cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}/reject")
    public ResponseEntity<?> rejectStore(@PathVariable String storeId, @RequestParam String reason) {
        try {
            storeService.rejectStore(storeId, reason);
            return ResponseEntity.ok(ApiResponse.ok("Từ chối cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}/status")
    public ResponseEntity<?> updateStoreStatus(@PathVariable String storeId, @RequestParam String status) {
        try {
            if (!Store.isValidStatus(status)) {
                String validStatuses = String.join(", ", Store.getValidStatuses());
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Status không hợp lệ: '" + status + "'. Chỉ chấp nhận: " + validStatuses)
                );
            }
            
            storeService.updateStoreStatus(storeId, status);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<?> softDelete(@PathVariable String storeId) {
        try {
            storeService.updateStoreStatus(storeId, "DELETED");
            return ResponseEntity.ok(ApiResponse.ok("Đã xóa (mềm) cửa hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
