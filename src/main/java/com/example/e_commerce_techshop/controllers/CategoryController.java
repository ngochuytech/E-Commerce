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
@Tag(name = "Category Management", description = "API cho quản lý danh mục sản phẩm")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lấy tất cả danh mục với phân trang", description = "Lấy danh sách các danh mục sản phẩm với tùy chọn phân trang và sắp xếp")
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mục trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "name") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc, desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDirection) {

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
    @Operation(summary = "Lấy tất cả danh mục không phân trang", description = "Lấy danh sách đầy đủ tất cả các danh mục sản phẩm mà không cần phân trang")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesWithoutPagination() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(categoryService::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy danh mục theo ID", description = "Lấy thông tin chi tiết của một danh mục cụ thể theo ID")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        Category category = categoryService.getCategoryById(id);
        CategoryDTO categoryDTO = categoryService.convertToDTO(category);
        return ResponseEntity.ok(categoryDTO);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Lấy danh mục theo tên", description = "Lấy thông tin chi tiết của một danh mục cụ thể theo tên")
    public ResponseEntity<CategoryDTO> getCategoryByName(
            @Parameter(description = "Tên danh mục", example = "Electronics") @PathVariable String name)
            throws Exception {
        Category category = categoryService.findByName(name);
        CategoryDTO categoryDTO = categoryService.convertToDTO(category);
        return ResponseEntity.ok(categoryDTO);
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Kiểm tra sự tồn tại của danh mục theo ID", description = "Kiểm tra xem một danh mục có tồn tại hay không dựa trên ID của nó")
    public ResponseEntity<Boolean> checkCategoryExists(
            @Parameter(description = "ID danh mục", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id) {
        boolean exists = categoryService.existsById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/name/{name}/exists")
    @Operation(summary = "Kiểm tra sự tồn tại của danh mục theo tên", description = "Kiểm tra xem một danh mục có tồn tại hay không dựa trên tên của nó")
    public ResponseEntity<Boolean> checkCategoryExistsByName(
            @Parameter(description = "Tên danh mục", example = "Electronics") @PathVariable String name) {
        boolean exists = categoryService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    @Operation(summary = "Tạo danh mục mới", description = "Tạo một danh mục mới với kiểm tra hợp lệ")
    public ResponseEntity<?> createCategory(
            @Parameter(description = "Thông tin danh mục") @Valid @RequestBody CategoryDTO categoryDTO,
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
    @Operation(summary = "Cập nhật danh mục", description = "Cập nhật thông tin của một danh mục cụ thể theo ID")
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "Category ID", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id,
            @Parameter(description = "Thông tin danh mục cập nhật") @Valid @RequestBody CategoryDTO categoryDTO,
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
    @Operation(summary = "Xóa danh mục", description = "Xóa một danh mục theo ID")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID danh mục", example = "670e8b8b9b3c4a1b2c3d4e5f") @PathVariable String id)
            throws Exception {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}