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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "Create product variant",
        description = "Create a new product variant with specifications like size, color, price, and multiple product images"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Product variant created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - validation errors or invalid data",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> createProductVariant(
        @Parameter(
            description = "Product variant information including product ID, size, color, price, stock quantity",
            required = true,
            content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))
        )
        @RequestPart("dto") @Valid ProductVariantDTO productVariantDTO,
        @Parameter(
            description = "Product variant images (multiple files supported)",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
        @Parameter(hidden = true) BindingResult result){
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

    @PostMapping(value = "/create-without-image")
    @Operation(
        summary = "Create product variant without images",
        description = "Create a new product variant with basic information (size, color, price) without uploading images. Images can be added later using the upload endpoint."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Product variant created successfully without images",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - validation errors or invalid product variant data",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> createProductVariantWithoutImage(
        @Parameter(
            description = "Product variant information including product ID, size, color, price, and stock quantity",
            required = true,
            content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))
        )
        @RequestBody @Valid ProductVariantDTO productVariantDTO) {
        try {
            productVariantService.createProductVariant(productVariantDTO);
            return ResponseEntity.ok(ApiResponse.ok("Tạo mẫu sản phẩm mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(value = "/upload-image/{productVariantid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload multiple images for product variant",
        description = "Upload multiple images for a specific product variant - adds to existing images"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Images uploaded successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - product variant not found or invalid image files",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> uploadProductVariantImage(
        @Parameter(description = "ID of the product variant to upload images for", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable("productVariantid") String productVariantId,
        @Parameter(
            description = "Multiple image files to upload for the product variant",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart(value = "images") List<MultipartFile> imageFiles){
        try {
            if (imageFiles == null || imageFiles.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("At least one image file is required"));
            }
            
            // Validate each file
            long maxSize = 10 * 1024 * 1024; // 10MB per file
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile.isEmpty()) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Empty image file found"));
                }
                
                // Validate file type
                String contentType = imageFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("Only image files are allowed"));
                }
                
                // Validate file size
                if (imageFile.getSize() > maxSize) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("File size too large. Maximum 10MB per file allowed"));
                }
            }
            
            // Use existing service method that accepts multiple images
            productVariantService.updateProductVariantWithImages(productVariantId, null, imageFiles);
            return ResponseEntity.ok(ApiResponse.ok("Upload nhiều ảnh sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping(value = "/add-colors/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Add color option to product variant",
        description = "Add a new color option to an existing product variant with its specific image"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Color option added successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - product variant not found or invalid color data",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> addProductVariantColors(
        @Parameter(description = "ID of the product variant to add color to", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable("id") String productVariantId,
        @Parameter(
            description = "Color option information including color name, hex code, and additional properties",
            required = true,
            content = @Content(schema = @Schema(implementation = ColorOption.class))
        )
        @Valid @RequestPart("dto") ColorOption colorOptionDTO,
        @Parameter(
            description = "Image file for this specific color variant",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart(value = "image") MultipartFile imageFile,
        @Parameter(hidden = true) BindingResult result){
        try {
            productVariantService.addProductVariantColors(productVariantId, colorOptionDTO, imageFile);
            return ResponseEntity.ok(ApiResponse.ok("Tạo màu sắc mẫu sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update product variant",
        description = "Update an existing product variant's information with optional single image replacement"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Product variant updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - validation errors or product variant not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> updateProductVariant(
        @Parameter(description = "ID of the product variant to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable("id") String productVariantId,
        @Parameter(
            description = "Updated product variant information",
            required = true,
            content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))
        )
        @Valid @RequestPart("dto") ProductVariantDTO productVariantDTO,
        @Parameter(
            description = "Optional replacement image for the product variant",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart(value = "image", required = false) MultipartFile imageFile,
        @Parameter(hidden = true) BindingResult result){
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
    @Operation(
        summary = "Update product variant with multiple images",
        description = "Update an existing product variant's information and replace all images with new ones"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Product variant updated with new images successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - validation errors or product variant not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> updateProductVariantWithImages(
        @Parameter(description = "ID of the product variant to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable("id") String productVariantId,
        @Parameter(
            description = "Updated product variant information",
            required = true,
            content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))
        )
        @Valid @RequestPart("dto") ProductVariantDTO productVariantDTO,
        @Parameter(
            description = "New images to replace all existing images (multiple files supported)",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
        @Parameter(hidden = true) BindingResult result){
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
    @Operation(
        summary = "Update specific color option",
        description = "Update a specific color option of a product variant including its image"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Color option updated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - product variant or color not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> updateProductVariantColors(
        @Parameter(description = "ID of the product variant", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable("id") String productVariantId,
        @Parameter(description = "ID of the specific color option to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2")
        @PathVariable("colorId") String colorId,
        @Parameter(
            description = "Updated color option information",
            required = true,
            content = @Content(schema = @Schema(implementation = ColorOption.class))
        )
        @Valid @RequestPart(value = "dto", required = false) ColorOption colorOptionDTO,
        @Parameter(
            description = "Optional replacement image for this color option",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestPart(value = "image", required = false) MultipartFile imageFile){
        try {
            productVariantService.updateProductVariantColors(productVariantId, colorId, colorOptionDTO, imageFile);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật màu sắc mẫu sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
        summary = "Delete product variant",
        description = "Soft delete a product variant by disabling it (sets status to inactive)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Product variant deleted successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - product variant not found",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> deleteProductVariant(
        @Parameter(description = "ID of the product variant to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable("id") String productVariantId){
        try {
            productVariantService.disableProduct(productVariantId);
            return ResponseEntity.ok(ApiResponse.ok("Xóa mẫu sản phẩm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
