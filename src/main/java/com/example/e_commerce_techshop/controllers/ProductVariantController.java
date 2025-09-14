package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.dtos.ProductVariantDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<?> createProductVaraint(@RequestPart("dto") @Valid ProductVariantDTO productVariantDTO,
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
            productVariantService.createProductVariant(productVariantDTO, imageFile);
            return ResponseEntity.ok(ApiResponse.ok("Tạo mẫu sản phẩm mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductVaraint(@PathVariable("id") String productVariantId,
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProductVariant(@PathVariable("id") String productVariantId){
        try{
            productVariantService.disableProduct(productVariantId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa mềm mẫu sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getByProductId(@PathVariable("id") String productId){
        try {
            List<ProductVariantResponse> productVariantResponses = productVariantService.getByProduct(productId);
            return ResponseEntity.ok(ApiResponse.ok(productVariantResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{name}")
    public ResponseEntity<?> getByCategory(@PathVariable("name") String category){
        try {
            List<ProductVariantResponse> productVariantResponses =  productVariantService.getByCategory(category);
            return ResponseEntity.ok(ApiResponse.ok(productVariantResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/category/{categoryName}/brand/{brandName}")
    public ResponseEntity<?> getByCategoryAndBrand(@PathVariable("categoryName") String category, @PathVariable("brandName") String brand){
        try {
            List<ProductVariantResponse> productVariantResponses =  productVariantService.getByCategoryAndBrand(category, brand);
            return ResponseEntity.ok(ApiResponse.ok(productVariantResponses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/filters")
    public ResponseEntity<?> getProductBySpecs(
            @RequestBody ProductFilterDTO filter) throws Exception {
        try{
            List<ProductVariantResponse> productResponeList = productVariantService.filterProducts(filter);
            return ResponseEntity.ok(ApiResponse.ok(productResponeList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
