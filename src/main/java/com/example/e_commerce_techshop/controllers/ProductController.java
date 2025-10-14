package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductResponse;
import com.example.e_commerce_techshop.services.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") String id){
        try {
            ProductResponse productResponse = productService.findProductById(id);
            return ResponseEntity.ok(ApiResponse.ok(productResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getProductByName(@RequestParam("name") String name){
        try {
            List<ProductResponse> responseList = productService.findProductByName(name);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{name}")
    public ResponseEntity<?> getProductByCategory(@PathVariable("name") String category){
        try {
            List<ProductResponse> responseList = productService.findProductByCategory(category);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}/brand/{brand}")
    public ResponseEntity<?> getProductByCategoryAndBrand(@PathVariable("category") String category, @PathVariable("brand") String brand){
        try {
            List<ProductResponse> responseList = productService.findProductByCategoryAndBrand(category, brand);
            return ResponseEntity.ok(ApiResponse.ok(responseList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
