package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ProductVariantDTO;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.product.IProductService;
import com.example.e_commerce_techshop.services.productVariant.IProductVariantService;
import com.example.e_commerce_techshop.services.store.IStoreService;

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
    private final IStoreService storeService;
    private final IProductService productService;

    @GetMapping("/{storeId}")
    @Operation(summary = "Lấy danh sách biến thể sản phẩm của cửa hàng", description = "Lấy danh sách tất cả biến thể sản phẩm cho một cửa hàng cụ thể")
    public ResponseEntity<?> getAllProductVariant(
            @PathVariable String storeId,
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
        Page<ProductVariant> productVariants = productVariantService.getAllProductVariantsB2C(storeId, status,
                pageable);
        Page<ProductVariantResponse> productVariantResponses = productVariants
                .map(ProductVariantResponse::fromProductVariant);
        return ResponseEntity.ok(ApiResponse.ok(productVariantResponses));
    }

    @GetMapping("/store/{storeId}/search")
    @Operation(summary = "Tìm kiếm biến thể sản phẩm trong cửa hàng", description = "Tìm kiếm biến thể sản phẩm theo tên trong một cửa hàng cụ thể")
    public ResponseEntity<?> searchProductVariants(
            @Parameter(description = "Store ID to search within", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Product name or keyword to search", required = true, example = "iPhone") @RequestParam String name,
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
        Page<ProductVariant> productVariants = productVariantService.searchByStoreAndName(storeId, name, status,
                pageable);
        Page<ProductVariantResponse> productVariantResponses = productVariants
                .map(ProductVariantResponse::fromProductVariant);
        return ResponseEntity.ok(ApiResponse.ok(productVariantResponses));
    }

    @GetMapping("/store/{storeId}/count-by-status")
    @Operation(summary = "Đếm số lượng variant theo trạng thái")
    public ResponseEntity<?> countProductVariantsByStatus(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("storeId") String storeId)
            throws Exception {
        Map<String, Long> countByStatus = productVariantService.countProductVariantsByStatus(storeId);
        return ResponseEntity.ok(ApiResponse.ok(countByStatus));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo biến thể sản phẩm", description = "Tạo một biến thể sản phẩm mới với các thông số kỹ thuật như kích thước, màu sắc, giá, và nhiều hình ảnh sản phẩm")
    public ResponseEntity<?> createProductVariant(
            @Parameter(description = "Thông tin biến thể sản phẩm bao gồm ID sản phẩm, kích thước, màu sắc, giá, số lượng tồn kho", required = true, content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))) @RequestPart("dto") @Valid ProductVariantDTO productVariantDTO,
            @Parameter(description = "Hình ảnh biến thể sản phẩm (hỗ trợ nhiều file)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
            @Parameter(description = "Chỉ số của hình ảnh chính trong danh sách hình ảnh", required = false) @RequestPart(value = "primaryImageIndex", required = false) String primaryImageIndex,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.error(String.join(",", errorMessages)));
        }
        // Kiểm tra shop có bị banned không
        Product product = productService.getProductById(productVariantDTO.getProductId());
        storeService.validateStoreNotBanned(product.getStore().getId());

        productVariantService.createProductVariant(productVariantDTO, imageFiles, primaryImageIndex);
        return ResponseEntity.ok(ApiResponse.ok("Tạo mẫu sản phẩm mới thành công!"));
    }

    @PostMapping(value = "/create-without-image")
    @Operation(summary = "Tạo mẫu sản phẩm không có hình ảnh", description = "Tạo một mẫu sản phẩm mới với thông tin cơ bản (kích thước, màu sắc, giá) mà không cần tải lên hình ảnh. Hình ảnh có thể được thêm sau bằng cách sử dụng endpoint tải lên.")
    public ResponseEntity<?> createProductVariantWithoutImage(
            @Parameter(description = "Product variant information including product ID, size, color, price, and stock quantity", required = true, content = @Content(schema = @Schema(implementation = ProductVariantDTO.class))) @RequestBody @Valid ProductVariantDTO productVariantDTO)
            throws Exception {
        // Kiểm tra shop có bị banned không
        Product product = productService.getProductById(productVariantDTO.getProductId());
        storeService.validateStoreNotBanned(product.getStore().getId());

        productVariantService.createProductVariant(productVariantDTO);
        return ResponseEntity.ok(ApiResponse.ok("Tạo mẫu sản phẩm mới thành công!"));
    }

    @PostMapping(value = "/add-colors/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Thêm tùy chọn màu sắc cho mẫu sản phẩm", description = "Thêm một tùy chọn màu sắc mới cho một mẫu sản phẩm hiện có cùng với hình ảnh cụ thể của nó")
    public ResponseEntity<?> addProductVariantColors(
            @Parameter(description = "ID of the product variant to add color to", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId,
            @Parameter(description = "Color option information including color name, hex code, and additional properties", required = true, content = @Content(schema = @Schema(implementation = ColorOption.class))) @Valid @RequestPart("dto") ColorOption colorOptionDTO,
            @Parameter(description = "Image file for this specific color variant", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "image") MultipartFile imageFile,
            @Parameter(hidden = true) BindingResult result) throws Exception {
        // Kiểm tra shop có bị banned không
        ProductVariant variant = productVariantService.getProductVariantById(productVariantId);
        storeService.validateStoreNotBanned(variant.getProduct().getStore().getId());

        productVariantService.addProductVariantColors(productVariantId, colorOptionDTO, imageFile);
        return ResponseEntity.ok(ApiResponse.ok("Tạo màu sắc mẫu sản phẩm thành công!"));
    }

    @PutMapping(value = "/update-colors/{id}/color/{colorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật tùy chọn màu sắc cụ thể", description = "Cập nhật một tùy chọn màu sắc cụ thể của một mẫu sản phẩm bao gồm hình ảnh của nó")
    public ResponseEntity<?> updateProductVariantColors(
            @Parameter(description = "ID of the product variant", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId,
            @Parameter(description = "ID of the specific color option to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable("colorId") String colorId,
            @Parameter(description = "Updated color option information", required = true, content = @Content(schema = @Schema(implementation = ColorOption.class))) @Valid @RequestPart(value = "dto", required = false) ColorOption colorOptionDTO,
            @Parameter(description = "Optional replacement image for this color option", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "image", required = false) MultipartFile imageFile)
            throws Exception {
        // Kiểm tra shop có bị banned không
        ProductVariant variant = productVariantService.getProductVariantById(productVariantId);
        storeService.validateStoreNotBanned(variant.getProduct().getStore().getId());

        productVariantService.updateProductVariantColors(productVariantId, colorId, colorOptionDTO, imageFile);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật màu sắc mẫu sản phẩm thành công!"));
    }

    @PutMapping(value = "/update-images/{variantId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật hình ảnh mẫu sản phẩm", description = "Cập nhật hình ảnh của một mẫu sản phẩm bằng cách thay thế các hình ảnh hiện có bằng những hình ảnh mới và chọn hình ảnh nào sẽ là hình ảnh chính")
    public ResponseEntity<?> updateProductVariantImages(
            @Parameter(description = "Id của product variant", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("variantId") String productVariantId,
            @Parameter(description = "File ảnh mới để cập nhật ảnh cho variant", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("images") List<MultipartFile> imageFiles,
            @Parameter(description = "Vị trí của ảnh chính bắt đầu từ 0(0-based, e.g., 0 for first image, 1 for second)", required = true, example = "0") @RequestParam(defaultValue = "0") int indexPrimary)
            throws Exception {
        // Kiểm tra shop có bị banned không
        ProductVariant variant = productVariantService.getProductVariantById(productVariantId);
        storeService.validateStoreNotBanned(variant.getProduct().getStore().getId());

        productVariantService.updateProductVariantImages(productVariantId, imageFiles, indexPrimary);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hình ảnh mẫu sản phẩm thành công!"));
    }

    @PutMapping("/update-stock/{id}")
    @Operation(summary = "Cập nhật số lượng tồn kho mẫu sản phẩm", description = "Cập nhật số lượng tồn kho của một mẫu sản phẩm. Chỉ ảnh hưởng đến số lượng tồn kho, giá không thay đổi.")
    public ResponseEntity<?> updateStock(
            @Parameter(description = "ID của mẫu sản phẩm cần cập nhật", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId,
            @Parameter(description = "Số lượng tồn kho mới", required = true, example = "100") @RequestBody int newStock)
            throws Exception {
        // Kiểm tra shop có bị banned không
        ProductVariant variant = productVariantService.getProductVariantById(productVariantId);
        storeService.validateStoreNotBanned(variant.getProduct().getStore().getId());

        productVariantService.updateStock(productVariantId, newStock);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật số lượng tồn kho thành công!"));
    }

    @PutMapping("/update-price/{id}")
    @Operation(summary = "Cập nhật giá bán mẫu sản phẩm", description = "Cập nhật giá bán của một mẫu sản phẩm. Chỉ ảnh hưởng đến giá, số lượng tồn kho không thay đổi.")
    public ResponseEntity<?> updatePrice(
            @Parameter(description = "ID của mẫu sản phẩm cần cập nhật", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId,
            @Parameter(description = "Giá bán mới tính bằng VND", required = true, example = "15000000") @RequestBody Long newPrice)
            throws Exception {
        // Kiểm tra shop có bị banned không
        ProductVariant variant = productVariantService.getProductVariantById(productVariantId);
        storeService.validateStoreNotBanned(variant.getProduct().getStore().getId());

        productVariantService.updatePrice(productVariantId, newPrice);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật giá bán thành công!"));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Xóa mẫu sản phẩm", description = "Xóa mềm một mẫu sản phẩm bằng cách vô hiệu hóa nó (đặt trạng thái thành không hoạt động)")
    public ResponseEntity<?> deleteProductVariant(
            @Parameter(description = "ID của mẫu sản phẩm cần xóa", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("id") String productVariantId)
            throws Exception {
        productVariantService.disableProduct(productVariantId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa mẫu sản phẩm thành công!"));
    }

    @DeleteMapping("/delete-color/{variantId}/color/{colorId}")
    @Operation(summary = "Xóa tùy chọn màu sắc khỏi mẫu sản phẩm", description = "Xóa một tùy chọn màu sắc cụ thể khỏi một mẫu sản phẩm và tự động cập nhật tổng số lượng tồn kho và giá của mẫu dựa trên các màu còn lại")
    public ResponseEntity<?> deleteProductVariantColor(
            @Parameter(description = "ID of the product variant", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable("variantId") String productVariantId,
            @Parameter(description = "ID of the color option to delete", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d2") @PathVariable("colorId") String colorId)
            throws Exception {
        productVariantService.removeProductVariantColor(productVariantId, colorId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa màu sắc mẫu sản phẩm thành công!"));
    }
}
