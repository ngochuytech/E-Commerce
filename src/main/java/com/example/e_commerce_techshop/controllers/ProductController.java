package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductResponse;
import com.example.e_commerce_techshop.services.product.IProductService;
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
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@Tag(name = "Product Browsing", description = "Public APIs for browsing and searching products")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve detailed product information by product ID")
    public ResponseEntity<?> getProductById(
            @Parameter(description = "Product ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("id") String id)
            throws Exception {
        ProductResponse productResponse = productService.findProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(productResponse));
    }

    @GetMapping("")
    @Operation(summary = "Search products by name", description = "Search for products using product name (partial match supported)")
    public ResponseEntity<?> getProductByName(
            @Parameter(description = "Product name to search for", example = "iPhone") @RequestParam("name") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responseList = productService.findProductByName(name, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/category/{name}")
    @Operation(summary = "Get products by category", description = "Retrieve all products belonging to a specific category")
    public ResponseEntity<?> getProductByCategory(
            @Parameter(description = "Category name", example = "Electronics") @PathVariable("name") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responseList = productService.findProductByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/category/{category}/brand/{brand}")
    @Operation(summary = "Get products by category and brand", description = "Retrieve products filtered by both category and brand")
    public ResponseEntity<?> getProductByCategoryAndBrand(
            @PathVariable("category") String category,
            @PathVariable("brand") String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responseList = productService.findProductByCategoryAndBrand(category, brand, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Get product by variant ID", description = "Retrieve the product associated with a specific product variant ID")
    public ResponseEntity<?> getProductByVariantId(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("variantId") String variantId)
            throws Exception {
        ProductResponse productResponse = ProductResponse.fromProduct(productService.getByVariant(variantId));
        return ResponseEntity.ok(ApiResponse.ok(productResponse));
    }
}
