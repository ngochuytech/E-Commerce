package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.b2c.store.StoreDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.StoreResponse;
import com.example.e_commerce_techshop.services.store.IStoreService;
import com.example.e_commerce_techshop.services.FileUploadService;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/stores")
@RequiredArgsConstructor
@Tag(name = "B2C Store Management", description = "Store management APIs for B2C sellers - Handle store creation, updates, approval process, and media uploads")
@SecurityRequirement(name = "bearerAuth")
public class B2CStoreController {
    
    private final IStoreService storeService;
    private final FileUploadService fileUploadService;

    // Get current user's stores
    @GetMapping("/my-stores")
    @Operation(
        summary = "Get my stores",
        description = "Retrieve all stores owned by the currently authenticated user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "User stores retrieved successfully",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = StoreResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - error retrieving stores",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getMyStores(
        @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
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
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create new store",
        description = "Create a new store with basic information and optional logo. Store will be in PENDING status awaiting admin approval"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - validation errors or store creation failed",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> createStore(
        @Parameter(
            description = "Store information including name, description, address, contact details",
            required = true,
            content = @Content(schema = @Schema(implementation = StoreDTO.class))
        )
        @Valid @RequestPart("storeDTO") StoreDTO storeDTO,
        @Parameter(
            description = "Optional store logo image file",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
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
    @Operation(
        summary = "Update store information",
        description = "Update basic information of an existing store"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - validation errors or store not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> updateStore(
        @Parameter(description = "ID of the store to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(
            description = "Updated store information",
            required = true,
            content = @Content(schema = @Schema(implementation = StoreDTO.class))
        )
        @RequestBody @Valid StoreDTO storeDTO) {
        try {    
            storeService.updateStore(storeId, storeDTO);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}/approve")
    @Operation(
        summary = "Approve store",
        description = "Approve a pending store registration (Admin function). Changes status from PENDING to APPROVED"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store approved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found or cannot be approved",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> approveStore(
        @Parameter(description = "ID of the store to approve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            storeService.approveStore(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Duyệt cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}/reject")
    @Operation(
        summary = "Reject store",
        description = "Reject a pending store registration with reason (Admin function). Changes status from PENDING to REJECTED"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store rejected successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found or cannot be rejected",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> rejectStore(
        @Parameter(description = "ID of the store to reject", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Reason for rejection", required = true, example = "Thông tin không đầy đủ")
        @RequestParam String reason) {
        try {
            storeService.rejectStore(storeId, reason);
            return ResponseEntity.ok(ApiResponse.ok("Từ chối cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{storeId}")
    @Operation(
        summary = "Soft delete store",
        description = "Soft delete a store by changing status to DELETED. Store data is preserved but becomes inactive"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store soft deleted successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found or cannot be deleted",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> softDelete(
        @Parameter(description = "ID of the store to soft delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            storeService.updateStoreStatus(storeId, "DELETED");
            return ResponseEntity.ok(ApiResponse.ok("Đã xóa (mềm) cửa hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Upload logo for specific store
    @PostMapping(value = "/{storeId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload store logo",
        description = "Upload or update the logo image for a specific store. Replaces existing logo if present"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Logo uploaded successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found, invalid file, or upload failed",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> uploadStoreLogo(
        @Parameter(description = "ID of the store to upload logo for", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(
            description = "Logo image file (JPG, PNG, GIF supported)",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
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
    @PostMapping(value = "/{storeId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload store banner",
        description = "Upload or update the banner image for a specific store. Used for store decoration and branding"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Banner uploaded successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found, invalid file, or upload failed",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> uploadStoreBanner(
        @Parameter(description = "ID of the store to upload banner for", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(
            description = "Banner image file (JPG, PNG, GIF supported, recommended size: 1200x400px)",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
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
