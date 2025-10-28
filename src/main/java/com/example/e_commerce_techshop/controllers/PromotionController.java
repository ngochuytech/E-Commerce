package com.example.e_commerce_techshop.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.PromotionResponse;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/promotions")
@RequiredArgsConstructor
@Tag(name = "Public Promotion APIs", description = "Public APIs to view promotions - No authentication required")
public class PromotionController {
    
    private final IPromotionService promotionService;

    @GetMapping("/active")
    @Operation(summary = "Get all active promotions", description = "Retrieve all currently active promotions across all stores and platform")
    public ResponseEntity<?> getActivePromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PromotionResponse> promotions = promotionService.getActivePromotions(pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/active/store/{storeId}")
    @Operation(summary = "Get active promotions by store", description = "Retrieve all currently active promotions for a specific store")
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
        Page<PromotionResponse> promotions = promotionService.getActivePromotionsByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(promotions));
    }

    @GetMapping("/platform")
    @Operation(summary = "Get all platform promotions", description = "Retrieve all active platform-wide promotions")
    public ResponseEntity<?> getPlatformPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Promotion> promotions = promotionService.getPlatformPromotions(pageable);
        Page<PromotionResponse> promotionResponses = promotions.map(PromotionResponse::fromPromotion);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponses));
    }

    @GetMapping("/{promotionId}")
    @Operation(summary = "Get promotion by ID", description = "Retrieve detailed information of a specific promotion")
    public ResponseEntity<?> getPromotionById(
            @Parameter(description = "ID of the promotion to retrieve", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId) throws Exception {
        Promotion promotion = promotionService.getPromotionById(promotionId);
        return ResponseEntity.ok(ApiResponse.ok(PromotionResponse.fromPromotion(promotion)));
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

    @GetMapping("/type/{type}")
    @Operation(summary = "Get promotions by type", description = "Retrieve all active promotions of a specific type (PERCENTAGE, FIXED_AMOUNT, etc.)")
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

    @GetMapping("/validate/{promotionId}")
    @Operation(summary = "Validate promotion", description = "Check if a promotion can be applied to an order with specified value")
    public ResponseEntity<?> validatePromotion(
            @Parameter(description = "ID of the promotion to validate", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Order value to validate against promotion conditions", required = true, example = "500000") @RequestParam Long orderValue) throws Exception {
        PromotionResponse promotionResponse = promotionService.validatePromotion(promotionId, orderValue);
        return ResponseEntity.ok(ApiResponse.ok(promotionResponse));
    }

    @GetMapping("/calculate-discount/{promotionId}")
    @Operation(summary = "Calculate discount amount", description = "Calculate the exact discount amount for a specific order value")
    public ResponseEntity<?> calculateDiscount(
            @Parameter(description = "ID of the promotion", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String promotionId,
            @Parameter(description = "Order value to calculate discount for", required = true, example = "500000") @RequestParam Long orderValue) throws Exception {
        Long discount = promotionService.calculateDiscount(promotionId, orderValue);
        return ResponseEntity.ok(ApiResponse.ok(discount));
    }
}
