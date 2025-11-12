package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/product-variants")
@RequiredArgsConstructor
@Tag(name = "Product Variant Browsing", description = "Public APIs for browsing product variants with specific colors, sizes, and pricing")
public class ProductVariantController {
    private final IProductVariantService productVariantService;

    @GetMapping("/{id}")
    @Operation(summary = "Get product variant by ID", description = "Retrieve detailed product variant information including pricing, inventory, and specifications")
    public ResponseEntity<?> getProductVariantById(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("id") String productVariantId)
            throws Exception {
        ProductVariantResponse response = productVariantService.getById(productVariantId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all variants of a product", description = "Retrieve all available variants (colors, sizes) for a specific product")
    public ResponseEntity<?> getProductVariantsByProduct(
            @Parameter(description = "Product ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("productId") String productId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariantResponse> responses = productVariantService.getByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get product variants by category", description = "Browse all product variants in a specific category")
    public ResponseEntity<?> getProductVariantsByCategory(
            @Parameter(description = "Category name", example = "Electronics") @PathVariable("category") String category,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariantResponse> responses = productVariantService.getByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/category/{category}/brand/{brand}")
    @Operation(summary = "Get product variants by category and brand", description = "Browse product variants filtered by both category and brand")
    public ResponseEntity<?> getProductVariantsByCategoryAndBrand(
            @Parameter(description = "Category name", example = "Electronics") @PathVariable("category") String category,
            @Parameter(description = "Brand name", example = "Apple") @PathVariable("brand") String brand,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariantResponse> responses = productVariantService.getByCategoryAndBrand(category, brand, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest product variants", description = "Retrieve recently added product variants with pagination and sorting")
    public ResponseEntity<?> getLatestProductVariants(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "15") @RequestParam(defaultValue = "15") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Page<ProductVariantResponse> response = productVariantService.getLatestProductVariants(page, size, sortBy,
                sortDir);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get product variants by store", description = "Browse all product variants from a specific store with pagination")
    public ResponseEntity<?> getProductVariantsByStore(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("storeId") String storeId,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Page<ProductVariantResponse> response = productVariantService.getByStore(storeId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search product variants by name", description = "Search for product variants by name with case-insensitive matching")
    public ResponseEntity<?> searchProductVariants(
            @Parameter(description = "Product name to search", example = "iPhone") @RequestParam("name") String name,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Page<ProductVariantResponse> response = productVariantService.searchByName(name, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}