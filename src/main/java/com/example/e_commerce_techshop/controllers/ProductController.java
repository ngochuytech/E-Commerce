package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductResponse;
import com.example.e_commerce_techshop.services.product.IProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@Tag(name = "Product Browsing", description = "API cho việc duyệt sản phẩm - Tìm kiếm và lọc sản phẩm")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Lấy sản phẩm theo ID", description = "Lấy thông tin chi tiết sản phẩm theo ID")
    public ResponseEntity<?> getProductById(
            @Parameter(description = "ID sản phẩm", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("id") String id)
            throws Exception {
        ProductResponse productResponse = productService.findProductById(id);
        return ResponseEntity.ok(ApiResponse.ok(productResponse));
    }

    @GetMapping("")
    @Operation(summary = "Tìm kiếm sản phẩm theo tên", description = "Tìm kiếm sản phẩm theo tên (hỗ trợ tìm kiếm gần đúng)")
    public ResponseEntity<?> getProductByName(
            @Parameter(description = "Tên sản phẩm cần tìm", example = "iPhone") @RequestParam("name") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responseList = productService.findProductByName(name, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/category/{name}")
    @Operation(summary = "Lấy sản phẩm theo danh mục", description = "Lấy tất cả sản phẩm thuộc một danh mục cụ thể")
    public ResponseEntity<?> getProductByCategory(
            @Parameter(description = "Tên danh mục", example = "Điện tử") @PathVariable("name") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responseList = productService.findProductByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/category/{category}/brand/{brand}")
    @Operation(summary = "Lấy sản phẩm theo danh mục và thương hiệu", description = "Lấy sản phẩm được lọc theo cả danh mục và thương hiệu")
    public ResponseEntity<?> getProductByCategoryAndBrand(
            @Parameter(description = "Tên danh mục", example = "Điện tử") @PathVariable("category") String category,
            @Parameter(description = "Tên thương hiệu", example = "Apple") @PathVariable("brand") String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> responseList = productService.findProductByCategoryAndBrand(category, brand, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Lấy sản phẩm theo ID biến thể", description = "Lấy sản phẩm liên quan đến một biến thể sản phẩm cụ thể")
    public ResponseEntity<?> getProductByVariantId(
            @Parameter(description = "ID biến thể sản phẩm", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("variantId") String variantId)
            throws Exception {
        ProductResponse productResponse = ProductResponse.fromProduct(productService.getByVariant(variantId));
        return ResponseEntity.ok(ApiResponse.ok(productResponse));
    }
}
