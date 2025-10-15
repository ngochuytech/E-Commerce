package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.BrandDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Get all brands with pagination", 
               description = "Retrieve paginated list of brands with sorting options")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Brands retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = BrandDTO.class))
            )
        )
    })
    public ResponseEntity<List<BrandDTO>> getAllBrands(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
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
    @Operation(summary = "Get all brands without pagination", 
               description = "Retrieve complete list of all brands")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "All brands retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = BrandDTO.class))
            )
        )
    })
    public ResponseEntity<List<BrandDTO>> getAllBrandsWithoutPagination() {
        List<Brand> brands = brandService.getAllBrands();
        List<BrandDTO> brandDTOs = brands.stream()
                .map(brandService::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(brandDTOs);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID", 
               description = "Retrieve a specific brand by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Brand found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BrandDTO.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Brand not found"
        )
    })
    public ResponseEntity<BrandDTO> getBrandById(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id) {
        try {
            Brand brand = brandService.getBrandById(id);
            BrandDTO brandDTO = brandService.convertToDTO(brand);
            return ResponseEntity.ok(brandDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @Operation(summary = "Create new brand", 
               description = "Create a new brand with validation")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Brand created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid data or brand already exists"
        )
    })
    public ResponseEntity<?> createBrand(
            @Parameter(description = "Brand information")
            @Valid @RequestBody BrandDTO brandDTO, 
            BindingResult result) {
        try {
            brandService.createBrand(brandDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tạo thương hiệu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating brand: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update brand", 
               description = "Update an existing brand by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Brand updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BrandDTO.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Brand not found"
        )
    })
    public ResponseEntity<?> updateBrand(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id, 
            @Parameter(description = "Updated brand information")
            @Valid @RequestBody BrandDTO brandDTO,
            BindingResult result) {
        
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        
        try {
            Brand updatedBrand = brandService.updateBrand(id, brandDTO);
            BrandDTO updatedBrandDTO = brandService.convertToDTO(updatedBrand);
            return ResponseEntity.ok(updatedBrandDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating brand: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete brand", 
               description = "Delete a brand by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Brand deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Brand not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Cannot delete brand (may be in use)"
        )
    })
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.noContent().build();
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if brand exists by ID", 
               description = "Check whether a brand exists by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Check completed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)
            )
        )
    })
    public ResponseEntity<Boolean> checkBrandExists(
            @Parameter(description = "Brand ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id) {
        boolean exists = brandService.existsById(id);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/name/{name}")
    @Operation(summary = "Get brand by name", 
               description = "Retrieve a specific brand by its name")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Brand found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BrandDTO.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Brand not found"
        )
    })
    public ResponseEntity<BrandDTO> getBrandByName(
            @Parameter(description = "Brand name", example = "Apple")
            @PathVariable String name) {
        try {
            Brand brand = brandService.findByName(name);
            BrandDTO brandDTO = brandService.convertToDTO(brand);
            return ResponseEntity.ok(brandDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/name/{name}/exists")
    @Operation(summary = "Check if brand exists by name", 
               description = "Check whether a brand exists by its name")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Check completed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class)
            )
        )
    })
    public ResponseEntity<Boolean> checkBrandExistsByName(
            @Parameter(description = "Brand name", example = "Apple")
            @PathVariable String name) {
        boolean exists = brandService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}