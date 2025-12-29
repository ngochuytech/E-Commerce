package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.BrandDTO;
import com.example.e_commerce_techshop.models.Brand;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "API cho quản lý thương hiệu sản phẩm")
@SecurityRequirement(name = "bearerAuth")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Lấy tất cả thương hiệu với phân trang", description = "Lấy danh sách các thương hiệu sản phẩm với tùy chọn phân trang và sắp xếp")
    public ResponseEntity<List<BrandDTO>> getAllBrands(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "name") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Brand> brandPage = brandService.getAllBrands(pageable);

        List<BrandDTO> brandDTOs = brandPage.getContent().stream()
                .map(brandService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(brandDTOs);
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả thương hiệu không phân trang", description = "Lấy danh sách đầy đủ tất cả các thương hiệu sản phẩm mà không cần phân trang")
    public ResponseEntity<List<BrandDTO>> getAllBrandsWithoutPagination() {
        List<Brand> brands = brandService.getAllBrands();
        List<BrandDTO> brandDTOs = brands.stream()
                .map(brandService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(brandDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thương hiệu theo ID", description = "Lấy thông tin chi tiết của một thương hiệu cụ thể theo ID")
    public ResponseEntity<BrandDTO> getBrandById(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        Brand brand = brandService.getBrandById(id);
        BrandDTO brandDTO = brandService.convertToDTO(brand);
        return ResponseEntity.ok(brandDTO);
    }

    @PostMapping
    @Operation(summary = "Tạo thương hiệu mới", description = "Tạo một thương hiệu mới với kiểm tra hợp lệ")
    public ResponseEntity<?> createBrand(
            @Parameter(description = "Thông tin thương hiệu") @Valid @RequestBody BrandDTO brandDTO,
            BindingResult result) throws Exception {
        brandService.createBrand(brandDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tạo thương hiệu thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thương hiệu", description = "Cập nhật thông tin của một thương hiệu cụ thể theo ID")
    public ResponseEntity<?> updateBrand(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id,
            @Parameter(description = "Thông tin thương hiệu cập nhật") @Valid @RequestBody BrandDTO brandDTO,
            BindingResult result) throws Exception {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        Brand updatedBrand = brandService.updateBrand(id, brandDTO);
        BrandDTO updatedBrandDTO = brandService.convertToDTO(updatedBrand);
        return ResponseEntity.ok(updatedBrandDTO);
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Kiểm tra sự tồn tại của thương hiệu theo ID", description = "Kiểm tra xem một thương hiệu có tồn tại hay không dựa trên ID của nó")
    public ResponseEntity<Boolean> checkBrandExists(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id) {
        boolean exists = brandService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Lấy thương hiệu theo tên", description = "Lấy thông tin chi tiết của một thương hiệu cụ thể theo tên")
    public ResponseEntity<BrandDTO> getBrandByName(
            @Parameter(description = "Tên thương hiệu", example = "Apple") @PathVariable String name) throws Exception {
        Brand brand = brandService.findByName(name);
        BrandDTO brandDTO = brandService.convertToDTO(brand);
        return ResponseEntity.ok(brandDTO);
    }

    @GetMapping("/name/{name}/exists")
    @Operation(summary = "Kiểm tra sự tồn tại của thương hiệu theo tên", description = "Kiểm tra xem một thương hiệu có tồn tại hay không dựa trên tên của nó")
    public ResponseEntity<Boolean> checkBrandExistsByName(
            @Parameter(description = "Tên thương hiệu", example = "Apple") @PathVariable String name) {
        boolean exists = brandService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thương hiệu", description = "Xóa một thương hiệu theo ID")
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}