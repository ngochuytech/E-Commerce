package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.CategoryDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Category;
import com.example.e_commerce_techshop.services.CategoryService;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categoryPage = categoryService.getAllCategories(pageable);
        
        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(categoryService::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categoryDTOs);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithoutPagination() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(categoryService::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categoryDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String id) {
        try {
            Category category = categoryService.getCategoryById(id);
            CategoryDTO categoryDTO = categoryService.convertToDTO(category);
            return ResponseEntity.ok(categoryDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryDTO> getCategoryByName(@PathVariable String name) {
        try {
            Category category = categoryService.findByName(name);
            CategoryDTO categoryDTO = categoryService.convertToDTO(category);
            return ResponseEntity.ok(categoryDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryDTO, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        
        try {
            Category createdCategory = categoryService.createCategory(categoryDTO);
            CategoryDTO createdCategoryDTO = categoryService.convertToDTO(createdCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategoryDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error creating category: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating category: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable String id, 
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result) {
        
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDTO);
            CategoryDTO updatedCategoryDTO = categoryService.convertToDTO(updatedCategory);
            return ResponseEntity.ok(updatedCategoryDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating category: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating category: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> checkCategoryExists(@PathVariable String id) {
        boolean exists = categoryService.existsById(id);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/name/{name}/exists")
    public ResponseEntity<Boolean> checkCategoryExistsByName(@PathVariable String name) {
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}