package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.dtos.ProductDTO;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductResponse;
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

    @GetMapping("/{storeId}")
    @Operation(summary = "Get all products", description = "Retrieve a list of all products for a specific store")
    public ResponseEntity<?> getAllProduct(@PathVariable("storeId") String storeId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.getAllProductsB2C(storeId, status, pageable);
        Page<ProductResponse> productResponses = products.map(ProductResponse::fromProduct);
        return ResponseEntity.ok(ApiResponse.ok(productResponses));

    }

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
