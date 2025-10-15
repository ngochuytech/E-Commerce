package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductResponse;
import com.example.e_commerce_techshop.services.product.IProductService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@Tag(name = "Product Browsing", description = "Public APIs for browsing and searching products")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", 
               description = "Retrieve detailed product information by product ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getProductById(
            @Parameter(description = "Product ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable("id") String id){
        try {
            ProductResponse productResponse = productService.findProductById(id);
            return ResponseEntity.ok(ApiResponse.ok(productResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("")
    @Operation(summary = "Search products by name", 
               description = "Search for products using product name (partial match supported)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Products found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Search failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        )
    })
    public ResponseEntity<?> getProductByName(
            @Parameter(description = "Product name to search for", example = "iPhone")
            @RequestParam("name") String name){
        try {
            List<ProductResponse> responseList = productService.findProductByName(name);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{name}")
    @Operation(summary = "Get products by category", 
               description = "Retrieve all products belonging to a specific category")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Products found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))
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
    public ResponseEntity<?> getProductByCategory(
            @Parameter(description = "Category name", example = "Electronics")
            @PathVariable("name") String category){
        try {
            List<ProductResponse> responseList = productService.findProductByCategory(category);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}/brand/{brand}")
    @Operation(summary = "Get products by category and brand", 
               description = "Retrieve products filtered by both category and brand")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Products found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))
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
    public ResponseEntity<?> getProductByCategoryAndBrand(
            @Parameter(description = "Category name", example = "Electronics")
            @PathVariable("category") String category, 
            @Parameter(description = "Brand name", example = "Apple")
            @PathVariable("brand") String brand){
        try {
            List<ProductResponse> responseList = productService.findProductByCategoryAndBrand(category, brand);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
