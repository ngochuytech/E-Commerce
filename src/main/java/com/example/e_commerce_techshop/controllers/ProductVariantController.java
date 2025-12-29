package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;
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
@RequestMapping("${api.prefix}/product-variants")
@RequiredArgsConstructor
@Tag(name = "Product Variant Browsing", description = "API cho việc duyệt biến thể sản phẩm - Tìm kiếm và lọc biến thể sản phẩm")
public class ProductVariantController {
    private final IProductVariantService productVariantService;

    @GetMapping("/{id}")
    @Operation(summary = "Lấy biến thể sản phẩm theo ID", description = "Lấy thông tin chi tiết biến thể sản phẩm bao gồm giá cả, tồn kho và thông số kỹ thuật")
    public ResponseEntity<?> getProductVariantById(
            @Parameter(description = "ID biến thể sản phẩm", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("id") String productVariantId)
            throws Exception {
        ProductVariantResponse response = productVariantService.getById(productVariantId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Lấy tất cả biến thể của một sản phẩm", description = "Lấy tất cả các biến thể có sẵn (màu sắc, kích thước) cho một sản phẩm cụ thể")
    public ResponseEntity<?> getProductVariantsByProduct(
            @Parameter(description = "ID sản phẩm", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("productId") String productId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariantResponse> responses = productVariantService.getByProduct(productId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Lấy biến thể sản phẩm theo danh mục", description = "Duyệt tất cả biến thể sản phẩm trong một danh mục cụ thể")
    public ResponseEntity<?> getProductVariantsByCategory(
            @Parameter(description = "Tên danh mục", example = "Điện tử") @PathVariable("category") String category,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariantResponse> responses = productVariantService.getByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/category/{category}/brand/{brand}")
    @Operation(summary = "Lấy biến thể sản phẩm theo danh mục và thương hiệu", description = "Duyệt biến thể sản phẩm được lọc theo cả danh mục và thương hiệu")
    public ResponseEntity<?> getProductVariantsByCategoryAndBrand(
            @Parameter(description = "Tên danh mục", example = "Điện tử") @PathVariable("category") String category,
            @Parameter(description = "Tên thương hiệu", example = "Apple") @PathVariable("brand") String brand,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariantResponse> responses = productVariantService.getByCategoryAndBrand(category, brand, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/latest")
    @Operation(summary = "Lấy biến thể sản phẩm mới nhất", description = "Lấy các biến thể sản phẩm mới được thêm gần đây với phân trang và sắp xếp")
    public ResponseEntity<?> getLatestProductVariants(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "15") @RequestParam(defaultValue = "15") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<ProductVariantResponse> response = productVariantService.getLatestProductVariants(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Lấy biến thể sản phẩm theo cửa hàng", description = "Duyệt tất cả biến thể sản phẩm từ một cửa hàng cụ thể với phân trang")
    public ResponseEntity<?> getProductVariantsByStore(
            @Parameter(description = "ID cửa hàng", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable("storeId") String storeId,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
        Page<ProductVariantResponse> response = productVariantService.getByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm biến thể sản phẩm theo tên", description = "Tìm kiếm biến thể sản phẩm theo tên với việc so khớp không phân biệt chữ hoa chữ thường")
    public ResponseEntity<?> searchProductVariants(
            @Parameter(description = "Tên biến thể sản phẩm cần tìm", example = "iPhone") @RequestParam("name") String name,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDir)
            throws Exception {
        Page<ProductVariantResponse> response = productVariantService.searchByName(name, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}