package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductResponse;
import com.example.e_commerce_techshop.services.product.IProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin Product Management", description = "APIs for admin to approve/reject products")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    private final IProductService productService;

    @GetMapping("/pending")
    @Operation(summary = "Get all pending products")
    public ResponseEntity<?> getPendingProducts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> products = productService.getPendingProducts(pageable);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @PutMapping("/{productId}/approve")
    @Operation(summary = "Approve product")
    public ResponseEntity<?> approveProduct(
            @Parameter(description = "Product ID") @PathVariable String productId) throws Exception {
        productService.updateStatus(productId, "APPROVED");
        return ResponseEntity.ok(ApiResponse.ok("Duyệt sản phẩm thành công!"));

    }

    @PutMapping("/{productId}/reject")
    @Operation(summary = "Reject product")
    public ResponseEntity<?> rejectProduct(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Rejection reason") @RequestParam String reason) throws Exception {
        productService.rejectProduct(productId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Từ chối sản phẩm thành công!"));
    }
}
