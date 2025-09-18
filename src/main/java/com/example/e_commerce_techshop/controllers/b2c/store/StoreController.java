package com.example.e_commerce_techshop.controllers.b2c.store;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/stores")
@RequiredArgsConstructor
public class StoreController {
    
    private final IStoreService storeService;

    // Store Management APIs
    @PostMapping("/create")
    public ResponseEntity<?> createStore(@Valid @RequestBody StoreDTO storeDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            
            StoreResponse storeResponse = storeService.createStore(storeDTO);
            return ResponseEntity.ok(ApiResponse.ok("Tạo cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<?> updateStore(@PathVariable String storeId, @RequestBody StoreDTO storeDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
            }
            
            StoreResponse storeResponse = storeService.updateStore(storeId, storeDTO);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<?> getStoreById(@PathVariable String storeId) {
        try {
            StoreResponse storeResponse = storeService.getStoreById(storeId);
            return ResponseEntity.ok(ApiResponse.ok(storeResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllStores() {
        List<StoreResponse> stores = storeService.getAllStores();
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getStoresByOwner(@PathVariable String ownerId) {
        List<StoreResponse> stores = storeService.getStoresByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    // Store Approval APIs
    @PutMapping("/{storeId}/approve")
    public ResponseEntity<?> approveStore(@PathVariable String storeId) {
        try {
            StoreResponse storeResponse = storeService.approveStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Duyệt cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}/reject")
    public ResponseEntity<?> rejectStore(@PathVariable String storeId, @RequestParam String reason) {
        try {
            // REASON lưu vào đâu ??
            StoreResponse storeResponse = storeService.rejectStore(storeId, reason);
            return ResponseEntity.ok(ApiResponse.ok("Từ chối cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

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

    // Store Status Management
    @PutMapping("/{storeId}/status")
    public ResponseEntity<?> updateStoreStatus(@PathVariable String storeId, @RequestParam String status) {
        try {
            storeService.updateStoreStatus(storeId, status);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
