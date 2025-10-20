package com.example.e_commerce_techshop.controllers;

import com.example.e_commerce_techshop.dtos.CategoryDTO;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing product categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories with pagination", description = "Retrieve paginated list of categories with sorting options")
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "name") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDirection) {

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
    @Operation(summary = "Get all categories without pagination", description = "Retrieve complete list of all categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithoutPagination() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(categoryService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        Category category = categoryService.getCategoryById(id);
        CategoryDTO categoryDTO = categoryService.convertToDTO(category);
        return ResponseEntity.ok(categoryDTO);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get category by name", description = "Retrieve a specific category by its name")
    public ResponseEntity<CategoryDTO> getCategoryByName(
            @Parameter(description = "Category name", example = "Electronics") @PathVariable String name)
            throws Exception {
        Category category = categoryService.findByName(name);
        CategoryDTO categoryDTO = categoryService.convertToDTO(category);
        return ResponseEntity.ok(categoryDTO);
    }

    @PostMapping
    @Operation(summary = "Create new category", description = "Create a new category with validation")
    public ResponseEntity<?> createCategory(
            @Parameter(description = "Category information") @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result) throws Exception {
        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        Category createdCategory = categoryService.createCategory(categoryDTO);
        CategoryDTO createdCategoryDTO = categoryService.convertToDTO(createdCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategoryDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category by ID")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id,
            @Parameter(description = "Updated category information") @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result) throws Exception {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        Category updatedCategory = categoryService.updateCategory(id, categoryDTO);
        CategoryDTO updatedCategoryDTO = categoryService.convertToDTO(updatedCategory);
        return ResponseEntity.ok(updatedCategoryDTO);

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category by ID")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if category exists by ID", description = "Check whether a category exists by its ID")
    public ResponseEntity<Boolean> checkCategoryExists(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id) {
        boolean exists = categoryService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/name/{name}/exists")
    @Operation(summary = "Check if category exists by name", description = "Check whether a category exists by its name")
    public ResponseEntity<Boolean> checkCategoryExistsByName(
            @Parameter(description = "Category name", example = "Electronics") @PathVariable String name) {
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}