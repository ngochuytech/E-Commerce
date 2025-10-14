package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final IProductVariantService productVariantService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductVariantById(@PathVariable("id") String productVariantId){
        try {
            ProductVariantResponse response = productVariantService.getById(productVariantId);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductVariantsByProduct(@PathVariable("productId") String productId){
        try {
            List<ProductVariantResponse> responses = productVariantService.getByProduct(productId);
            return ResponseEntity.ok(ApiResponse.ok(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getProductVariantsByCategory(@PathVariable("category") String category){
        try {
            List<ProductVariantResponse> responses = productVariantService.getByCategory(category);
            return ResponseEntity.ok(ApiResponse.ok(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}/brand/{brand}")
    public ResponseEntity<?> getProductVariantsByCategoryAndBrand(@PathVariable("category") String category,
                                                                 @PathVariable("brand") String brand){
        try {
            List<ProductVariantResponse> responses = productVariantService.getByCategoryAndBrand(category, brand);
            return ResponseEntity.ok(ApiResponse.ok(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestProductVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ProductVariantResponse> response = productVariantService.getLatestProductVariants(page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getProductVariantsByStore(
            @PathVariable("storeId") String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Page<ProductVariantResponse> response = productVariantService.getByStore(storeId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    
}