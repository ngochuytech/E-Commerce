package com.example.e_commerce_techshop.services;

import com.example.e_commerce_techshop.dtos.CategoryDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Category;
import com.example.e_commerce_techshop.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public Category getCategoryById(String id) throws DataNotFoundException {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Category not found with id: " + id));
    }
    
    public Category createCategory(CategoryDTO categoryDTO) throws IllegalArgumentException {
        // Check if category name already exists
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .build();
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(String id, CategoryDTO categoryDTO) throws DataNotFoundException, IllegalArgumentException {
        Category existingCategory = getCategoryById(id);
        
        // Check if new name already exists (excluding current category)
        if (!existingCategory.getName().equals(categoryDTO.getName()) && 
            categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());
        return categoryRepository.save(existingCategory);
    }
    
    public void deleteCategory(String id) throws DataNotFoundException {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
    
    public boolean existsById(String id) {
        return categoryRepository.existsById(id);
    }
    
    public Category findByName(String name) throws DataNotFoundException {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new DataNotFoundException("Category not found with name: " + name));
    }
    
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    public CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}