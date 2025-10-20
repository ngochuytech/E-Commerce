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
@Tag(name = "Brand Management", description = "APIs for managing product brands")
@SecurityRequirement(name = "bearerAuth")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Get all brands with pagination", description = "Retrieve paginated list of brands with sorting options")
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
    @Operation(summary = "Get all brands without pagination", description = "Retrieve complete list of all brands")
    public ResponseEntity<List<BrandDTO>> getAllBrandsWithoutPagination() {
        List<Brand> brands = brandService.getAllBrands();
        List<BrandDTO> brandDTOs = brands.stream()
                .map(brandService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(brandDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID", description = "Retrieve a specific brand by its ID")
    public ResponseEntity<BrandDTO> getBrandById(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        Brand brand = brandService.getBrandById(id);
        BrandDTO brandDTO = brandService.convertToDTO(brand);
        return ResponseEntity.ok(brandDTO);
    }

    @PostMapping
    @Operation(summary = "Create new brand", description = "Create a new brand with validation")
    public ResponseEntity<?> createBrand(
            @Parameter(description = "Brand information") @Valid @RequestBody BrandDTO brandDTO,
            BindingResult result) throws Exception {
        brandService.createBrand(brandDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tạo thương hiệu thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update brand", description = "Update an existing brand by ID")
    public ResponseEntity<?> updateBrand(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id,
            @Parameter(description = "Updated brand information") @Valid @RequestBody BrandDTO brandDTO,
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete brand", description = "Delete a brand by ID")
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if brand exists by ID", description = "Check whether a brand exists by its ID")
    public ResponseEntity<Boolean> checkBrandExists(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id) {
        boolean exists = brandService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get brand by name", description = "Retrieve a specific brand by its name")
    public ResponseEntity<BrandDTO> getBrandByName(
            @Parameter(description = "Brand name", example = "Apple") @PathVariable String name) throws Exception {
        Brand brand = brandService.findByName(name);
        BrandDTO brandDTO = brandService.convertToDTO(brand);
        return ResponseEntity.ok(brandDTO);
    }

    @GetMapping("/name/{name}/exists")
    @Operation(summary = "Check if brand exists by name", description = "Check whether a brand exists by its name")
    public ResponseEntity<Boolean> checkBrandExistsByName(
            @Parameter(description = "Brand name", example = "Apple") @PathVariable String name) {
        boolean exists = brandService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}