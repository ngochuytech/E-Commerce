package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.ProductDTO;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.product.IProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/products")
@RequiredArgsConstructor
@Tag(name = "B2C Product Management", description = "Product management APIs for B2C stores - Handle product creation, updates, and catalog management")
@SecurityRequirement(name = "bearerAuth")
public class B2CProductController {
    private final IProductService productService;

    @PostMapping("/create")
    @Operation(summary = "Create new product", description = "Create a new product for the store with comprehensive product information including name, description, price, category, and specifications")
    public ResponseEntity<?> createProduct(
            @Parameter(description = "Product information including name, description, price, category, brand, images, and specifications", required = true, content = @Content(schema = @Schema(implementation = ProductDTO.class))) @RequestBody @Valid ProductDTO productDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }
        productService.createProduct(productDTO);
        return ResponseEntity.ok(ApiResponse.ok("Tạo sản phẩm mới thành công!"));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update existing product", description = "Update an existing product's information including name, description, price, category, brand, images, and specifications")
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "ID of the product to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productId,
            @Parameter(description = "Updated product information including name, description, price, category, brand, images, and specifications", required = true, content = @Content(schema = @Schema(implementation = ProductDTO.class))) @RequestBody @Valid ProductDTO productDTO,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, String.join(", ", errorMessages)));
        }
        productService.updateProduct(productId, productDTO);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật sản phẩm thành công!"));
    }
}
