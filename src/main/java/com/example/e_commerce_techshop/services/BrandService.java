package com.example.e_commerce_techshop.services;

import com.example.e_commerce_techshop.dtos.BrandDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Brand;
import com.example.e_commerce_techshop.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {
    
    private final BrandRepository brandRepository;
    
    public Page<Brand> getAllBrands(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }
    
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
    
    public Brand getBrandById(String id) throws DataNotFoundException {
        return brandRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Brand not found with id: " + id));
    }
    
    public Brand createBrand(BrandDTO brandDTO) throws IllegalArgumentException {
        // Check if brand name already exists
        if (brandRepository.existsByName(brandDTO.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brandDTO.getName() + "' already exists");
        }
        
        Brand brand = Brand.builder()
                .name(brandDTO.getName())
                .build();
        return brandRepository.save(brand);
    }
    
    public Brand updateBrand(String id, BrandDTO brandDTO) throws DataNotFoundException, IllegalArgumentException {
        Brand existingBrand = getBrandById(id);
        
        // Check if new name already exists (excluding current brand)
        if (!existingBrand.getName().equals(brandDTO.getName()) && 
            brandRepository.existsByName(brandDTO.getName())) {
            throw new IllegalArgumentException("Brand with name '" + brandDTO.getName() + "' already exists");
        }
        
        existingBrand.setName(brandDTO.getName());
        return brandRepository.save(existingBrand);
    }
    
    public void deleteBrand(String id) throws DataNotFoundException {
        Brand brand = getBrandById(id);
        brandRepository.delete(brand);
    }
    
    public boolean existsById(String id) {
        return brandRepository.existsById(id);
    }
    
    public Brand findByName(String name) throws DataNotFoundException {
        return brandRepository.findByName(name)
                .orElseThrow(() -> new DataNotFoundException("Brand not found with name: " + name));
    }
    
    public boolean existsByName(String name) {
        return brandRepository.existsByName(name);
    }
    
    public BrandDTO convertToDTO(Brand brand) {
        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }
}