package com.example.e_commerce_techshop.controllers.b2c.store;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;
import com.example.e_commerce_techshop.services.FileUploadService;
import com.example.e_commerce_techshop.models.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/stores")
@RequiredArgsConstructor
public class StoreController {
    
    private final IStoreService storeService;
    private final FileUploadService fileUploadService;

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
            
            // Lấy user ID từ JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName(); // Lấy user ID từ JWT
            
            // Set owner_id từ JWT, bỏ qua owner_id trong JSON
            storeDTO.setOwnerId(currentUserId);
            
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
            // ✅ VALIDATE STATUS TRƯỚC KHI GỌI SERVICE
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

    // Soft delete: anyone with owner or admin can delete
    @DeleteMapping("/{storeId}")
    public ResponseEntity<?> softDelete(@PathVariable String storeId) {
        try {
            storeService.updateStoreStatus(storeId, "DELETED");
            return ResponseEntity.ok(ApiResponse.ok("Đã xóa (mềm) cửa hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Upload logo for specific store
    @PostMapping("/{storeId}/logo")
    public ResponseEntity<?> uploadStoreLogo(@PathVariable String storeId, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            System.out.println("Uploading logo for store: " + storeId);
            
            // Validate store exists and user has permission (from JWT)
            StoreResponse store = storeService.getStoreById(storeId);
            System.out.println("Found store: " + store.getName());
            
            // Delete old logo if exists
            if (store.getLogoUrl() != null && !store.getLogoUrl().isEmpty()) {
                System.out.println("Deleting old logo: " + store.getLogoUrl());
                fileUploadService.deleteFile(store.getLogoUrl());
            }
            
            // Upload new logo
            System.out.println("Uploading new logo file...");
            String logoUrl = fileUploadService.uploadFile(file, "stores");
            System.out.println("Logo uploaded successfully: " + logoUrl);
            
            // Update store with new logo URL
            StoreDTO updateDTO = new StoreDTO();
            updateDTO.setName(store.getName());
            updateDTO.setDescription(store.getDescription());
            updateDTO.setLogoUrl(logoUrl);
            updateDTO.setBannerUrl(store.getBannerUrl());
            updateDTO.setStatus(store.getStatus());
            
            System.out.println("Updating store with new logo URL...");
            storeService.updateStore(storeId, updateDTO);
            System.out.println("Store updated successfully");
            
            return ResponseEntity.ok(ApiResponse.ok(logoUrl));
        } catch (Exception e) {
            System.err.println("Error uploading logo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Upload banner for specific store
    @PostMapping("/{storeId}/banner")
    public ResponseEntity<?> uploadStoreBanner(@PathVariable String storeId, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            System.out.println("Uploading banner for store: " + storeId);
            
            // Validate store exists and user has permission (from JWT)
            StoreResponse store = storeService.getStoreById(storeId);
            System.out.println("Found store: " + store.getName());
            
            // Delete old banner if exists
            if (store.getBannerUrl() != null && !store.getBannerUrl().isEmpty()) {
                System.out.println("Deleting old banner: " + store.getBannerUrl());
                fileUploadService.deleteFile(store.getBannerUrl());
            }
            
            // Upload new banner
            System.out.println("Uploading new banner file...");
            String bannerUrl = fileUploadService.uploadFile(file, "stores");
            System.out.println("Banner uploaded successfully: " + bannerUrl);
            
            // Update store with new banner URL
            StoreDTO updateDTO = new StoreDTO();
            updateDTO.setName(store.getName());
            updateDTO.setDescription(store.getDescription());
            updateDTO.setLogoUrl(store.getLogoUrl());
            updateDTO.setBannerUrl(bannerUrl);
            updateDTO.setStatus(store.getStatus());
            
            System.out.println("Updating store with new banner URL...");
            storeService.updateStore(storeId, updateDTO);
            System.out.println("Store updated successfully");
            
            return ResponseEntity.ok(ApiResponse.ok(bannerUrl));
        } catch (Exception e) {
            System.err.println("Error uploading banner: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
