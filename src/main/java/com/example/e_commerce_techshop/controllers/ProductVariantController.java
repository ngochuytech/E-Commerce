package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/product-variants")
@RequiredArgsConstructor
@Tag(name = "Product Variant Browsing", description = "Public APIs for browsing product variants with specific colors, sizes, and pricing")
public class ProductVariantController {
    private final IProductVariantService productVariantService;

    @GetMapping("/{id}")
    @Operation(summary = "Get product variant by ID", 
               description = "Retrieve detailed product variant information including pricing, inventory, and specifications")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product variant found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductVariantResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Product variant not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getProductVariantById(
            @Parameter(description = "Product variant ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable("id") String productVariantId){
        try {
            ProductVariantResponse response = productVariantService.getById(productVariantId);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all variants of a product", 
               description = "Retrieve all available variants (colors, sizes) for a specific product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product variants found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProductVariantResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Product not found or no variants available",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getProductVariantsByProduct(
            @Parameter(description = "Product ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable("productId") String productId){
        try {
            List<ProductVariantResponse> responses = productVariantService.getByProduct(productId);
            return ResponseEntity.ok(ApiResponse.ok(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get product variants by category", 
               description = "Browse all product variants in a specific category")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product variants found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProductVariantResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Category not found or search failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getProductVariantsByCategory(
            @Parameter(description = "Category name", example = "Electronics")
            @PathVariable("category") String category){
        try {
            List<ProductVariantResponse> responses = productVariantService.getByCategory(category);
            return ResponseEntity.ok(ApiResponse.ok(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}/brand/{brand}")
    @Operation(summary = "Get product variants by category and brand", 
               description = "Browse product variants filtered by both category and brand")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product variants found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProductVariantResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Category/brand not found or search failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getProductVariantsByCategoryAndBrand(
            @Parameter(description = "Category name", example = "Electronics")
            @PathVariable("category") String category,
            @Parameter(description = "Brand name", example = "Apple")
            @PathVariable("brand") String brand){
        try {
            List<ProductVariantResponse> responses = productVariantService.getByCategoryAndBrand(category, brand);
            return ResponseEntity.ok(ApiResponse.ok(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest product variants", 
               description = "Retrieve recently added product variants with pagination and sorting")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Latest product variants retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getLatestProductVariants(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "2")
            @RequestParam(defaultValue = "2") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ProductVariantResponse> response = productVariantService.getLatestProductVariants(page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get product variants by store", 
               description = "Browse all product variants from a specific store with pagination")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Store product variants retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Store not found or invalid parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getProductVariantsByStore(
            @Parameter(description = "Store ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable("storeId") String storeId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ProductVariantResponse> response = productVariantService.getByStore(storeId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    
}