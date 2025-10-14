package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.ProductVariantDTO;
import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/product-variants")
@RequiredArgsConstructor
public class B2CProductVariantController {
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

    @PostMapping(value = "/add-colors/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductVariantColors(@PathVariable("id") String productVariantId,
                                                     @Valid @RequestPart("dto") ColorOption colorOptionDTO,
                                                     @RequestPart(value = "image") MultipartFile imageFile,
                                                     BindingResult result){
        try {
            productVariantService.addProductVariantColors(productVariantId, colorOptionDTO, imageFile);
            return ResponseEntity.ok(ApiResponse.ok("Tạo màu sắc mẫu sản phẩm thành công!"));
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

    @PutMapping(value = "/update-colors/{id}/color/{colorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductVariantColors(@PathVariable("id") String productVariantId,
                                                        @PathVariable("colorId") String colorId,
                                                        @Valid @RequestPart("dto") ColorOption colorOptionDTO,
                                                        @RequestPart(value = "image", required = false) MultipartFile imageFile){
        try {
            productVariantService.updateProductVariantColors(productVariantId, colorId, colorOptionDTO, imageFile);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật màu sắc mẫu sản phẩm thành công!"));
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
}
