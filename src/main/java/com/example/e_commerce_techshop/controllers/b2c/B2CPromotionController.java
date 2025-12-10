package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;
import com.example.e_commerce_techshop.services.store.IStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/promotions")
@RequiredArgsConstructor
@Tag(name = "B2C Promotion Management", description = "Promotion management APIs for B2C stores - Handle discount campaigns, coupon codes, and promotional offers")
@SecurityRequirement(name = "bearerAuth")
public class B2CPromotionController {

    private final IPromotionService promotionService;
    private final IStoreService storeService;

    private void validateUserStore(String ownerId, String storeId) throws Exception {
        Store store = storeService.getStoreByIdAndOwnerId(storeId, ownerId);

        if (store == null) {
            throw new RuntimeException("Bạn không có quyền truy cập cửa hàng này");
        }
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get promotions by store", description = "Retrieve all promotions for a specific store (both active and inactive)")
    public ResponseEntity<?> getPromotionsByStore(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/active")
    @Operation(summary = "Get active promotions by store", description = "Retrieve all active promotions for a specific store")
    public ResponseEntity<?> getActivePromotionsByStore(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStoreAndStatus(storeId, Promotion.PromotionStatus.ACTIVE.name(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/inactive")
    @Operation(summary = "Get inactive promotions by store", description = "Retrieve all inactive promotions for a specific store")
    public ResponseEntity<?> getInactivePromotionsByStore(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStoreAndStatus(storeId, Promotion.PromotionStatus.INACTIVE.name(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/expired")
    @Operation(summary = "Get expired promotions by store", description = "Retrieve all expired promotions for a specific store")
    public ResponseEntity<?> getExpiredPromotionsByStore(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getExpiredPromotionsByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/deleted")
    @Operation(summary = "Get deleted promotions by store", description = "Retrieve all deleted promotions for a specific store")
    public ResponseEntity<?> getDeletedPromotionsByStore(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByStoreAndStatus(storeId, Promotion.PromotionStatus.DELETED.name(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}/count-by-status")
    @Operation(summary = "Đếm số lượng khuyến mãi theo trạng thái")
    public ResponseEntity<?> countPromotionsByStatus(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId) throws Exception {
        Map<String, Long> counts = promotionService.countPromotionsByStatus(storeId);
        return ResponseEntity.ok(ApiResponse.ok(counts));
    }

    // Promotion Management APIs - Store specific (Auto-assign issuer=STORE)
    @PostMapping("/store/{storeId}")
    @Operation(summary = "Create store promotion", description = "Create promotion for a specific store. Issuer and storeId will be auto-assigned. Only store owner can use this.")
    public ResponseEntity<?> createStorePromotion(
            @Parameter(description = "ID of the store creating the promotion", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Promotion information (issuer and storeId not required)", required = true, content = @Content(schema = @Schema(implementation = CreatePromotionDTO.class))) @Valid @RequestBody CreatePromotionDTO createPromotionDTO,
            @Parameter(hidden = true) BindingResult result,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        validateUserStore(currentUser.getId(), storeId);

        promotionService.createStorePromotion(createPromotionDTO, storeId);
        return ResponseEntity.ok(ApiResponse.ok("Tạo khuyến mãi thành công"));
    }

    @PutMapping("/{promotionId}")
    @Operation(summary = "Update promotion", description = "Update an existing promotion's information including discount rules and validity period")
    public ResponseEntity<?> updatePromotion(
            @Parameter(description = "ID of the promotion to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Updated promotion information", required = true, content = @Content(schema = @Schema(implementation = PromotionDTO.class))) @RequestBody PromotionDTO promotionDTO,
            @Parameter(hidden = true) BindingResult result,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        promotionService.updateStorePromotion(promotionId, promotionDTO, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật khuyến mãi thành công"));
    }

    // Promotion Status Management APIs
    @PutMapping("/{promotionId}/activate")
    @Operation(summary = "Activate promotion", description = "Activate a promotion to make it available for customers to use")
    public ResponseEntity<?> activatePromotion(
            @Parameter(description = "ID of the promotion to activate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser)
            throws Exception {
        promotionService.activateStorePromotion(promotionId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Kích hoạt khuyến mãi thành công!"));
    }

    @PutMapping("/{promotionId}/deactivate")
    @Operation(summary = "Deactivate promotion", description = "Deactivate a promotion to stop customers from using it")
    public ResponseEntity<?> deactivatePromotion(
            @Parameter(description = "ID of the promotion to deactivate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser)
            throws Exception {
        promotionService.deactivateStorePromotion(promotionId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Vô hiệu hóa khuyến mãi thành công!"));
    }

    @DeleteMapping("/{promotionId}")
    @Operation(summary = "Delete promotion", description = "Delete a promotion (permanently remove from system)")
    public ResponseEntity<?> deletePromotion(
            @Parameter(description = "ID of the promotion to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser)
            throws Exception {
        promotionService.deleteStorePromotion(promotionId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa khuyến mãi thành công!"));
    }

}
