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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ProductVariantDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/product-variants")
@RequiredArgsConstructor
@Tag(name = "B2C Product Variant Management", description = "Product variant management APIs for B2C stores - Handle product variants, colors, sizes, and image uploads")
@SecurityRequirement(name = "bearerAuth")
public class B2CProductVariantController {
    private final IProductVariantService productVariantService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create product variant", description = "Create a new product variant with specifications like size, color, price, and multiple product images")
    public ResponseEntity<?> createProductVariant(
            @Parameter(description = "Product variant information including product ID, size, color, price, stock quantity", required = true, content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))) @RequestPart("dto") @Valid ProductVariantDTO productVariantDTO,
            @Parameter(description = "Product variant images (multiple files supported)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.error(String.join(",", errorMessages)));
        }
        productVariantService.createProductVariant(productVariantDTO, imageFiles);
        return ResponseEntity.ok(ApiResponse.ok("Tạo mẫu sản phẩm mới thành công!"));
    }

    @PostMapping(value = "/create-without-image")
    @Operation(summary = "Create product variant without images", description = "Create a new product variant with basic information (size, color, price) without uploading images. Images can be added later using the upload endpoint.")
    public ResponseEntity<?> createProductVariantWithoutImage(
            @Parameter(description = "Product variant information including product ID, size, color, price, and stock quantity", required = true, content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))) @RequestBody @Valid ProductVariantDTO productVariantDTO)
            throws Exception {
        productVariantService.createProductVariant(productVariantDTO);
        return ResponseEntity.ok(ApiResponse.ok("Tạo mẫu sản phẩm mới thành công!"));
    }

    @PostMapping(value = "/add-colors/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add color option to product variant", description = "Add a new color option to an existing product variant with its specific image")
    public ResponseEntity<?> addProductVariantColors(
            @Parameter(description = "ID of the product variant to add color to", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId,
            @Parameter(description = "Color option information including color name, hex code, and additional properties", required = true, content = @Content(schema = @Schema(implementation = ColorOption.class))) @Valid @RequestPart("dto") ColorOption colorOptionDTO,
            @Parameter(description = "Image file for this specific color variant", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "image") MultipartFile imageFile,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        productVariantService.addProductVariantColors(productVariantId, colorOptionDTO, imageFile);
        return ResponseEntity.ok(ApiResponse.ok("Tạo màu sắc mẫu sản phẩm thành công!"));
    }

    @PutMapping(value = "/update-colors/{id}/color/{colorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update specific color option", description = "Update a specific color option of a product variant including its image")
    public ResponseEntity<?> updateProductVariantColors(
            @Parameter(description = "ID of the product variant", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId,
            @Parameter(description = "ID of the specific color option to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable("colorId") String colorId,
            @Parameter(description = "Updated color option information", required = true, content = @Content(schema = @Schema(implementation = ColorOption.class))) @Valid @RequestPart(value = "dto", required = false) ColorOption colorOptionDTO,
            @Parameter(description = "Optional replacement image for this color option", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "image", required = false) MultipartFile imageFile)
            throws Exception {
        productVariantService.updateProductVariantColors(productVariantId, colorId, colorOptionDTO, imageFile);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật màu sắc mẫu sản phẩm thành công!"));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete product variant", description = "Soft delete a product variant by disabling it (sets status to inactive)")
    public ResponseEntity<?> deleteProductVariant(
            @Parameter(description = "ID of the product variant to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId)
            throws Exception {
        productVariantService.disableProduct(productVariantId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa mẫu sản phẩm thành công!"));
    }
}
