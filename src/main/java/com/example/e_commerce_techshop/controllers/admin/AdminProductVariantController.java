package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/product-variants")
@RequiredArgsConstructor
@Tag(name = "Admin ProductVariant Management")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminProductVariantController {
    private final IProductVariantService productVariantService;

    @GetMapping("/pending")
    @Operation(summary = "Get all pending product variants")
    public ResponseEntity<?> getPendingVariants(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariantResponse> pendingVariants = productVariantService
                .getVariantsByStatus(ProductVariant.VariantStatus.PENDING.name(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(
                pendingVariants));
    }

    @PutMapping("/{variantId}/approve")
    @Operation(summary = "Approve product variant")
    public ResponseEntity<?> approveVariant(@PathVariable String variantId) throws Exception {
        productVariantService.updateVariantStatus(variantId, ProductVariant.VariantStatus.APPROVED.name());
        return ResponseEntity.ok(ApiResponse.ok("Duyệt biến thể thành công!"));
    }

    @PutMapping("/{variantId}/reject")
    @Operation(summary = "Reject product variant")
    public ResponseEntity<?> rejectVariant(
            @PathVariable String variantId,
            @RequestParam String reason) throws Exception {
        productVariantService.rejectVariant(variantId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối biến thể thành công!"));
    }
}
