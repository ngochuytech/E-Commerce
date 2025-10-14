package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;
import com.example.e_commerce_techshop.services.FileUploadService;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/stores")
@RequiredArgsConstructor
public class B2CStoreController {
    
    private final IStoreService storeService;
    private final FileUploadService fileUploadService;

    // Get current user's stores
    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(@AuthenticationPrincipal User currentUser) {
        try {
            List<Store> stores = storeService.getStoresByOwner(currentUser.getId());
            
            List<StoreResponse> storeResponses = stores.stream()
                .map(StoreResponse::fromStore)
                .toList();
            
            return ResponseEntity.ok(ApiResponse.ok(storeResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Store Management APIs
    @PostMapping("/create")
    public ResponseEntity<?> createStore(@Valid @RequestPart("storeDTO") StoreDTO storeDTO,
                                        @RequestPart(value = "logo", required = false) MultipartFile logo) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = authentication.getName();
            
            // Set owner_id từ JWT, bỏ qua owner_id trong JSON
            storeDTO.setOwnerId(currentUserId);
            
            storeService.createStore(storeDTO, logo);
            return ResponseEntity.ok(ApiResponse.ok("Tạo cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<?> updateStore(@PathVariable String storeId, @RequestBody @Valid StoreDTO storeDTO) {
        try {    
            storeService.updateStore(storeId, storeDTO);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
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
            
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật logo thành công: "));
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
            storeService.uploadBanner(storeId, file);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật banner thành công"));
        } catch (Exception e) {
            System.err.println("Error uploading banner: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
