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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Get all categories with pagination", 
               description = "Retrieve paginated list of categories with sorting options")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))
            )
        )
    })
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
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
        Page<Category> categoryPage = categoryService.getAllCategories(pageable);
        
        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(categoryService::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categoryDTOs);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all categories without pagination", 
               description = "Retrieve complete list of all categories")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "All categories retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))
            )
        )
    })
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithoutPagination() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(categoryService::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categoryDTOs);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", 
               description = "Retrieve a specific category by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Category found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Category not found"
        )
    })
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id) {
        try {
            Category category = categoryService.getCategoryById(id);
            CategoryDTO categoryDTO = categoryService.convertToDTO(category);
            return ResponseEntity.ok(categoryDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/name/{name}")
    @Operation(summary = "Get category by name", 
               description = "Retrieve a specific category by its name")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Category found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Category not found"
        )
    })
    public ResponseEntity<CategoryDTO> getCategoryByName(
            @Parameter(description = "Category name", example = "Electronics")
            @PathVariable String name) {
        try {
            Category category = categoryService.findByName(name);
            CategoryDTO categoryDTO = categoryService.convertToDTO(category);
            return ResponseEntity.ok(categoryDTO);
        } catch (DataNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @Operation(summary = "Create new category", 
               description = "Create a new category with validation")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Category created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid data or category already exists"
        )
    })
    public ResponseEntity<?> createCategory(
            @Parameter(description = "Category information")
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
    @Operation(summary = "Update category", 
               description = "Update an existing category by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Category updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryDTO.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Category not found"
        )
    })
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id, 
            @Parameter(description = "Updated category information")
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
    @Operation(summary = "Delete category", 
               description = "Delete a category by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Category deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Category not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Cannot delete category (may be in use)"
        )
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id) {
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
    @Operation(summary = "Check if category exists by ID", 
               description = "Check whether a category exists by its ID")
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
    public ResponseEntity<Boolean> checkCategoryExists(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f")
            @PathVariable String id) {
        boolean exists = categoryService.existsById(id);
        return ResponseEntity.ok(exists);
    }
    
    @GetMapping("/name/{name}/exists")
    @Operation(summary = "Check if category exists by name", 
               description = "Check whether a category exists by its name")
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
    public ResponseEntity<Boolean> checkCategoryExistsByName(
            @Parameter(description = "Category name", example = "Electronics")
            @PathVariable String name) {
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }
}