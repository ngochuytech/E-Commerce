package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.dtos.b2c.store.UpdateStoreDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/stores")
@RequiredArgsConstructor
@Tag(name = "B2C Store Management", description = "Store management APIs for B2C sellers - Handle store creation, updates, approval process, and media uploads")
@SecurityRequirement(name = "bearerAuth")
public class B2CStoreController {

    private final IStoreService storeService;

    // Get current user's stores
    @GetMapping("/my-stores")
    @Operation(summary = "Get my stores", description = "Retrieve all stores owned by the currently authenticated user")
    public ResponseEntity<?> getMyStores(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        List<Store> stores = storeService.getStoresByOwner(currentUser.getId());

        List<StoreResponse> storeResponses = stores.stream()
                .map(StoreResponse::fromStore)
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(storeResponses));
    }

    // Store Management APIs
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new store", description = "Create a new store with basic information and optional logo. Store will be in PENDING status awaiting admin approval")
    public ResponseEntity<?> createStore(
            @Parameter(description = "Store information including name, description, address, contact details", required = true, content = @Content(schema = @Schema(implementation = StoreDTO.class))) @Valid @RequestPart("storeDTO") StoreDTO storeDTO,
            @Parameter(description = "Optional store logo image file", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "logo", required = false) MultipartFile logo,
            @AuthenticationPrincipal User currentUser) throws Exception {
        // Set owner_id từ JWT, bỏ qua owner_id trong JSON
        storeDTO.setOwnerId(currentUser.getId());

        storeService.createStore(storeDTO, logo);
        return ResponseEntity.ok(ApiResponse.ok("Tạo cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}")
    @Operation(summary = "Update store information", description = "Update basic information of an existing store")
    public ResponseEntity<?> updateStore(
            @Parameter(description = "ID of the store to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Updated store information", required = true, content = @Content(schema = @Schema(implementation = UpdateStoreDTO.class))) @RequestBody @Valid UpdateStoreDTO updateStoreDTO)
            throws Exception {
        storeService.updateStore(storeId, updateStoreDTO);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}/approve")
    @Operation(summary = "Approve store", description = "Approve a pending store registration (Admin function). Changes status from PENDING to APPROVED")
    public ResponseEntity<?> approveStore(
            @Parameter(description = "ID of the store to approve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        storeService.approveStore(storeId);
        return ResponseEntity.ok(ApiResponse.ok("Duyệt cửa hàng thành công!"));
    }

    @PutMapping("/{storeId}/reject")
    @Operation(summary = "Reject store", description = "Reject a pending store registration with reason (Admin function). Changes status from PENDING to REJECTED")
    public ResponseEntity<?> rejectStore(
            @Parameter(description = "ID of the store to reject", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Reason for rejection", required = true, example = "Thông tin không đầy đủ") @RequestParam String reason)
            throws Exception {
        storeService.rejectStore(storeId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối cửa hàng thành công!"));
    }

    @DeleteMapping("/{storeId}")
    @Operation(summary = "Soft delete store", description = "Soft delete a store by changing status to DELETED. Store data is preserved but becomes inactive")
    public ResponseEntity<?> softDelete(
            @Parameter(description = "ID of the store to soft delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        storeService.updateStoreStatus(storeId, "DELETED");
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa (mềm) cửa hàng"));
    }

    // Upload logo for specific store
    @PutMapping(value = "/{storeId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update store logo", description = "Update the logo image for a specific store. Only available for APPROVED stores. Replaces existing logo if present")
    public ResponseEntity<?> updateStoreLogo(
            @Parameter(description = "ID of the store to update logo for", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Logo image file (JPG, PNG, GIF supported)", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file)
            throws Exception {
        storeService.updateStoreLogo(storeId, file);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật logo thành công!"));
    }

    // Upload banner for specific store
    @PutMapping(value = "/{storeId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update store banner", description = "Update the banner image for a specific store. Only available for APPROVED stores. Used for store decoration and branding")
    public ResponseEntity<?> updateStoreBanner(
            @Parameter(description = "ID of the store to update banner for", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Banner image file (JPG, PNG, GIF supported, recommended size: 1200x400px)", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file)
            throws Exception {
        storeService.updateStoreBanner(storeId, file);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật banner thành công!"));
    }
}
