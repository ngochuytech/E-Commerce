package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.dtos.ProductVariantDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {
    private final IProductVariantService productVariantService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductVariant(@RequestPart("dto") @Valid ProductVariantDTO productVariantDTO,
                                                  @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                                  BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ApiResponse.error(String.join(",", errorMessages)));
            }
            productVariantService.createProductVariant(productVariantDTO, imageFiles);
            return ResponseEntity.ok(ApiResponse.ok("Tạo mẫu sản phẩm mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductVariant(@PathVariable("id") String productVariantId,
                                                  @Valid @RequestPart("dto") ProductVariantDTO productVariantDTO,
                                                  @RequestPart(value = "image", required = false) MultipartFile imageFile,
                                                  BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ApiResponse.error(String.join(",", errorMessages)));
            }
            productVariantService.updateProductVariant(productVariantId, productVariantDTO, imageFile);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật mẫu sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping(value = "/update-with-images/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductVariantWithImages(@PathVariable("id") String productVariantId,
                                                           @Valid @RequestPart("dto") ProductVariantDTO productVariantDTO,
                                                           @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                                           BindingResult result){
        try {
            if(result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(ApiResponse.error(String.join(",", errorMessages)));
            }
            productVariantService.updateProductVariantWithImages(productVariantId, productVariantDTO, imageFiles);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật mẫu sản phẩm với nhiều ảnh thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProductVariant(@PathVariable("id") String productVariantId){
        try {
            productVariantService.disableProduct(productVariantId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa mẫu sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

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

    @PostMapping("/filter")
    public ResponseEntity<?> filterProducts(@RequestBody ProductFilterDTO filterDTO){
        try {
            List<ProductVariantResponse> responses = productVariantService.filterProducts(filterDTO);
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