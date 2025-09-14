package com.example.e_commerce_techshop.services.inventory;

import com.example.e_commerce_techshop.dtos.b2c.inventory.ProductVariantDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.repositories.ProductRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService implements IInventoryService {
    
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Override
    public ProductVariantResponse createProductVariant(ProductVariantDTO variantDTO) throws Exception {
        // Validate product exists
        Product product = productRepository.findById(variantDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm"));

        // Create product variant
        ProductVariant variant = ProductVariant.builder()
                .name(variantDTO.getName())
                .image_url(variantDTO.getImageUrl())
                .price(variantDTO.getPrice())
                .description(variantDTO.getDescription())
                .stock(variantDTO.getStock())
                .product(product)
                .build();

        ProductVariant savedVariant = productVariantRepository.save(variant);
        return ProductVariantResponse.fromProductVariant(savedVariant);
    }

    @Override
    public ProductVariantResponse updateProductVariant(String variantId, ProductVariantDTO variantDTO) throws Exception {
        ProductVariant existingVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy biến thể sản phẩm"));

        // Update fields
        existingVariant.setName(variantDTO.getName());
        existingVariant.setImage_url(variantDTO.getImageUrl());
        existingVariant.setPrice(variantDTO.getPrice());
        existingVariant.setDescription(variantDTO.getDescription());
        existingVariant.setStock(variantDTO.getStock());

        // Update product if provided
        if (variantDTO.getProductId() != null) {
            Product product = productRepository.findById(variantDTO.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm"));
            existingVariant.setProduct(product);
        }

        ProductVariant updatedVariant = productVariantRepository.save(existingVariant);
        return ProductVariantResponse.fromProductVariant(updatedVariant);
    }

    @Override
    public ProductVariantResponse getProductVariantById(String variantId) throws Exception {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy biến thể sản phẩm"));
        return ProductVariantResponse.fromProductVariant(variant);
    }

    @Override
    public List<ProductVariantResponse> getProductVariantsByProduct(String productId) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        return variants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public void deleteProductVariant(String variantId) throws Exception {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy biến thể sản phẩm"));
        productVariantRepository.delete(variant);
    }

    @Override
    public ProductVariantResponse updateStock(String variantId, Integer newStock) throws Exception {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy biến thể sản phẩm"));
        
        variant.setStock(newStock);
        ProductVariant updatedVariant = productVariantRepository.save(variant);
        return ProductVariantResponse.fromProductVariant(updatedVariant);
    }

    @Override
    public ProductVariantResponse increaseStock(String variantId, Integer quantity) throws Exception {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy biến thể sản phẩm"));
        
        variant.setStock(variant.getStock() + quantity);
        ProductVariant updatedVariant = productVariantRepository.save(variant);
        return ProductVariantResponse.fromProductVariant(updatedVariant);
    }

    @Override
    public ProductVariantResponse decreaseStock(String variantId, Integer quantity) throws Exception {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy biến thể sản phẩm"));
        
        if (variant.getStock() < quantity) {
            throw new Exception("Không đủ hàng tồn kho. Số lượng hiện tại: " + variant.getStock());
        }
        
        variant.setStock(variant.getStock() - quantity);
        ProductVariant updatedVariant = productVariantRepository.save(variant);
        return ProductVariantResponse.fromProductVariant(updatedVariant);
    }

    @Override
    public List<ProductVariantResponse> getLowStockItems(Integer threshold) {
        List<ProductVariant> variants = productVariantRepository.findByStockLessThan(threshold);
        return variants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getOutOfStockItems() {
        List<ProductVariant> variants = productVariantRepository.findByStockEquals(0);
        return variants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getAllVariants() {
        List<ProductVariant> variants = productVariantRepository.findAll();
        return variants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }
}
