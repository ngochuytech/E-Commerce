package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import com.example.e_commerce_techshop.dtos.b2c.promotion.CreatePromotionDTO;
import com.example.e_commerce_techshop.dtos.b2c.promotion.PromotionDTO;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Promotion Management", description = "Promotion management APIs for Platform Admin - Create platform-wide promotions")
@SecurityRequirement(name = "bearerAuth")
public class AdminPromotionController {
    private final IPromotionService promotionService;

    @PostMapping("/platform")
    @Operation(summary = "Create platform promotion", description = "Create platform-wide promotion. Issuer will be auto-assigned as PLATFORM and storeId will be null. Only platform admin can use this.")
    public ResponseEntity<?> createPlatformPromotion(
            @Parameter(description = "Promotion information (issuer and storeId not required - auto-assigned)", required = true, content = @Content(schema = @Schema(implementation = CreatePromotionDTO.class))) 
            @Valid @RequestBody CreatePromotionDTO createPromotionDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        promotionService.createPlatformPromotion(createPromotionDTO);
        return ResponseEntity.ok(ApiResponse.ok("Tạo khuyến mãi thành công"));
    }

    @PutMapping("/platform/{promotionId}")
    @Operation(summary = "Update platform promotion", description = "Update an existing platform-wide promotion. Only platform admin can use this.")
    public ResponseEntity<?> updatePlatformPromotion(
            @Parameter(description = "ID of the promotion to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Promotion information", required = true, content = @Content(schema = @Schema(implementation = PromotionDTO.class))) 
            @Valid @RequestBody PromotionDTO promotionDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }

        promotionService.updatePlatformPromotion(promotionId, promotionDTO);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật khuyến mãi thành công"));
    }

    // View all promotions (Admin oversight)
    @GetMapping
    @Operation(summary = "Get all promotions (Admin)", description = "Retrieve list of all promotions across all stores and platform - Admin oversight")
    public ResponseEntity<?> getAllPromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Promotion> promotions = promotionService.getAllPromotions(pageable);
        Page<PromotionResponse> promotionResponses = promotions.map(PromotionResponse::fromPromotion);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponses));
    }

    @GetMapping("/{promotionId}")
    @Operation(summary = "Get promotion by ID (Admin)", description = "Retrieve detailed information of a specific promotion - Admin view")
    public ResponseEntity<?> getPromotionById(
            @Parameter(description = "ID of the promotion to retrieve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        Promotion promotion = promotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(ApiResponse.ok(PromotionResponse.fromPromotion(promotion)));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get promotions by store (Admin)", description = "Retrieve all promotions for a specific store - Admin oversight")
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

    // Promotion Management
    @DeleteMapping("/{promotionId}")
    @Operation(summary = "Delete promotion (Admin)", description = "Permanently delete any promotion - platform or store")
    public ResponseEntity<?> deletePromotion(
            @Parameter(description = "ID of the promotion to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.deletePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa khuyến mãi thành công!"));
    }

    // Promotion Status Management
    @PutMapping("/{promotionId}/activate")
    @Operation(summary = "Activate promotion (Admin)", description = "Activate any promotion to make it available")
    public ResponseEntity<?> activatePromotion(
            @Parameter(description = "ID of the promotion to activate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.activatePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Kích hoạt khuyến mãi thành công!"));
    }

    @PutMapping("/{promotionId}/deactivate")
    @Operation(summary = "Deactivate promotion (Admin)", description = "Deactivate any promotion to stop it from being used")
    public ResponseEntity<?> deactivatePromotion(
            @Parameter(description = "ID of the promotion to deactivate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId)
            throws Exception {
        promotionService.deactivatePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Vô hiệu hóa khuyến mãi thành công!"));
    }

    @GetMapping("/reports/expired")
    @Operation(summary = "Get expired platform promotions (Admin)", description = "Retrieve platform promotions that have expired - Admin cleanup view")
    public ResponseEntity<?> getExpiredPromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getExpiredPlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/deleted")
    @Operation(summary = "Get deleted platform promotions (Admin)", description = "Retrieve platform promotions that have been soft deleted - Admin view only")
    public ResponseEntity<?> getDeletedPromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getDeletedPlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/active")
    @Operation(summary = "Get active platform promotions (Admin)", description = "Retrieve platform promotions that are currently active - Admin view only")
    public ResponseEntity<?> getActivePromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getActivePlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/inactive")
    @Operation(summary = "Get inactive platform promotions (Admin)", description = "Retrieve platform promotions that are currently inactive - Admin view only")
    public ResponseEntity<?> getInactivePromotions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getInactivePlatformPromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/reports/type/{type}")
    @Operation(summary = "Get promotions by type (Admin)", description = "Retrieve all promotions of a specific type - Admin analytics")
    public ResponseEntity<?> getPromotionsByType(
            @Parameter(description = "Type of promotion to filter by", required = true, example = "PERCENTAGE") @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getPromotionsByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

}
