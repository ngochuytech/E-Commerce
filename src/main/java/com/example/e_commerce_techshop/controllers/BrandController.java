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

@RestController
@RequestMapping("${api.prefix}/brands")
@RequiredArgsConstructor
public class BrandController {
    
    private final BrandService brandService;
    
    @GetMapping
    public ResponseEntity<List<BrandDTO>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
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
    public ResponseEntity<List<BrandDTO>> getAllBrandsWithoutPagination() {
        List<Brand> brands = brandService.getAllBrands();
        List<BrandDTO> brandDTOs = brands.stream()
                .map(brandService::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(brandDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable String id) {
        try {
            Brand brand = brandService.getBrandById(id);
            BrandDTO brandDTO = brandService.convertToDTO(brand);
            return ResponseEntity.ok(brandDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createBrand(@Valid @RequestBody BrandDTO brandDTO, BindingResult result) {
        try {
            brandService.createBrand(brandDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Tạo thương hiệu thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating brand: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBrand(
            @PathVariable String id, 
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
    public ResponseEntity<Void> deleteBrand(@PathVariable String id) {
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
    public ResponseEntity<Boolean> checkBrandExists(@PathVariable String id) {
        boolean exists = brandService.existsById(id);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<BrandDTO> getBrandByName(@PathVariable String name) {
        try {
            Brand brand = brandService.findByName(name);
            BrandDTO brandDTO = brandService.convertToDTO(brand);
            return ResponseEntity.ok(brandDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/name/{name}/exists")
    public ResponseEntity<Boolean> checkBrandExistsByName(@PathVariable String name) {
        boolean exists = brandService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}