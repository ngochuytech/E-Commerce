package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/b2c/promotions")
@RequiredArgsConstructor
@Tag(name = "B2C Promotion Management", description = "Promotion management APIs for B2C stores - Handle discount campaigns, coupon codes, and promotional offers")
@SecurityRequirement(name = "bearerAuth")
public class B2CPromotionController {

    private final IPromotionService promotionService;

    // Promotion Management APIs
    @PostMapping("/create")
    @Operation(summary = "Create new promotion", description = "Create a new promotional campaign with discount rules, validity period, and applicable conditions")
    public ResponseEntity<?> createPromotion(
            @Parameter(description = "Promotion information including name, discount type, value, start/end dates, and conditions", required = true, content = @Content(schema = @Schema(implementation = PromotionDTO.class))) @RequestBody PromotionDTO promotionDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        PromotionResponse promotionResponse = promotionService.createPromotion(promotionDTO);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponse));
    }

    @PutMapping("/{promotionId}")
    @Operation(summary = "Update promotion", description = "Update an existing promotion's information including discount rules and validity period")
    public ResponseEntity<?> updatePromotion(
            @Parameter(description = "ID of the promotion to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Updated promotion information", required = true, content = @Content(schema = @Schema(implementation = PromotionDTO.class))) @RequestBody PromotionDTO promotionDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        promotionService.updatePromotion(promotionId, promotionDTO);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật khuyến mãi thành công"));
    }

    @GetMapping("/{promotionId}")
    @Operation(summary = "Get promotion by ID", description = "Retrieve detailed information of a specific promotion")
    public ResponseEntity<?> getPromotionById(
            @Parameter(description = "ID of the promotion to retrieve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        PromotionResponse promotionResponse = promotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponse));
    }

    @GetMapping
    @Operation(summary = "Get all promotions", description = "Retrieve list of all promotions across all stores")
    public ResponseEntity<?> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get promotions by store", description = "Retrieve all promotions for a specific store")
    public ResponseEntity<?> getPromotionsByStore(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId) {
        List<PromotionResponse> promotions = promotionService.getPromotionsByStore(storeId);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @DeleteMapping("/{promotionId}")
    @Operation(summary = "Delete promotion", description = "Delete a promotion (permanently remove from system)")
    public ResponseEntity<?> deletePromotion(
            @Parameter(description = "ID of the promotion to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.deletePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa khuyến mãi thành công!"));
    }

    // Promotion Status Management APIs
    @PutMapping("/{promotionId}/activate")
    @Operation(summary = "Activate promotion", description = "Activate a promotion to make it available for customers to use")
    public ResponseEntity<?> activatePromotion(
            @Parameter(description = "ID of the promotion to activate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.activatePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Kích hoạt khuyến mãi thành công!"));
    }

    @PutMapping("/{promotionId}/deactivate")
    @Operation(summary = "Deactivate promotion", description = "Deactivate a promotion to stop customers from using it")
    public ResponseEntity<?> deactivatePromotion(
            @Parameter(description = "ID of the promotion to deactivate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.deactivatePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Vô hiệu hóa khuyến mãi thành công!"));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active promotions", description = "Retrieve all currently active promotions across all stores")
    public ResponseEntity<?> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/active/store/{storeId}")
    @Operation(summary = "Get active promotions by store", description = "Retrieve all currently active promotions for a specific store")
    public ResponseEntity<?> getActivePromotionsByStore(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId) {
        List<PromotionResponse> promotions = promotionService.getActivePromotionsByStore(storeId);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    // Promotion Validation & Application APIs
    @PostMapping("/{promotionId}/validate")
    @Operation(summary = "Validate promotion", description = "Validate if a promotion can be applied to an order with specified value")
    public ResponseEntity<?> validatePromotion(
            @Parameter(description = "ID of the promotion to validate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Order value to validate against promotion conditions", required = true, example = "500000") @RequestParam Long orderValue)
            throws Exception {
        promotionService.validatePromotion(promotionId, orderValue);
        return ResponseEntity.ok(ApiResponse.ok("Khuyến mãi hợp lệ!"));
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

    // Promotion Reports APIs
    @GetMapping("/reports/expired")
    @Operation(summary = "Get expired promotions", description = "Retrieve all promotions that have passed their expiration date")
    public ResponseEntity<?> getExpiredPromotions() {
        List<PromotionResponse> promotions = promotionService.getExpiredPromotions();
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/type/{type}")
    @Operation(summary = "Get promotions by type", description = "Retrieve all promotions of a specific type (PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y, etc.)")
    public ResponseEntity<?> getPromotionsByType(
            @Parameter(description = "Type of promotion to filter by", required = true, example = "PERCENTAGE") @PathVariable String type) {
        List<PromotionResponse> promotions = promotionService.getPromotionsByType(type);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }
}
