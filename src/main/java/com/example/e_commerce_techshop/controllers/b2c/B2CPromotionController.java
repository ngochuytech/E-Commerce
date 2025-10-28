package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;
import com.example.e_commerce_techshop.services.store.IStoreService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

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

    @PostMapping("/{promotionId}/calculate-discount")
    @Operation(summary = "Calculate discount amount", description = "Calculate the exact discount amount that will be applied for a specific order value")
    public ResponseEntity<?> calculateDiscount(
            @Parameter(description = "ID of the promotion", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Order value to calculate discount for", required = true, example = "500000") @RequestParam Long orderValue)
            throws Exception {
        Long discount = promotionService.calculateDiscount(promotionId, orderValue);
        return ResponseEntity.ok(ApiResponse.ok(discount));
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

    @GetMapping("/{promotionId}")
    @Operation(summary = "Get promotion by ID", description = "Retrieve detailed information of a specific promotion")
    public ResponseEntity<?> getPromotionById(
            @Parameter(description = "ID of the promotion to retrieve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        Promotion promotionResponse = promotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(ApiResponse.ok(PromotionResponse.fromPromotion(promotionResponse)));
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
