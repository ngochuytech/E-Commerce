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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "Get pending stores",
        description = "Retrieve all stores that are waiting for admin approval"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved pending stores",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = StoreResponse.class))
            )
        )
    })
    public ResponseEntity<?> getPendingStores() {
        List<StoreResponse> stores = storeService.getPendingStores();
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @GetMapping("/approved")
    @Operation(
        summary = "Get approved stores",
        description = "Retrieve all stores that have been approved by admin"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved approved stores",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = StoreResponse.class))
            )
        )
    })
    public ResponseEntity<?> getApprovedStores() {
        List<StoreResponse> stores = storeService.getApprovedStores();
        return ResponseEntity.ok(ApiResponse.ok(stores));
    }

    @PutMapping("/{storeId}/approve")
    @Operation(
        summary = "Approve store",
        description = "Approve a pending store registration"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store approved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found or invalid status",
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
        description = "Reject a pending store registration with a reason"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store rejected successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found or invalid status",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> rejectStore(
        @Parameter(description = "ID of the store to reject", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Reason for rejection", required = true, example = "Không đủ điều kiện kinh doanh")
        @RequestParam String reason) {
        try {
            storeService.rejectStore(storeId, reason);
            return ResponseEntity.ok(ApiResponse.ok("Từ chối cửa hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{storeId}/status")
    @Operation(
        summary = "Update store status",
        description = "Update the status of a store (activate/deactivate)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store status updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found or invalid status",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> updateStoreStatus(
        @Parameter(description = "ID of the store to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "New status for the store", required = true, example = "ACTIVE")
        @RequestParam String status) {
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
    @Operation(
        summary = "Soft delete store",
        description = "Soft delete a store by marking it as deleted"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Store soft deleted successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - store not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> softDeleteStore(
        @Parameter(description = "ID of the store to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            storeService.updateStoreStatus(storeId, "DELETED");
            return ResponseEntity.ok(ApiResponse.ok("Đã xóa (mềm) cửa hàng"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
