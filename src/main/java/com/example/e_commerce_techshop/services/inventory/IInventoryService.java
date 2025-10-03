package com.example.e_commerce_techshop.services.inventory;

import com.example.e_commerce_techshop.dtos.b2c.inventory.ProductVariantDTO;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;

import java.util.List;

public interface IInventoryService {
    // ProductVariant Management
    ProductVariantResponse createProductVariant(ProductVariantDTO variantDTO) throws Exception;
    ProductVariantResponse updateProductVariant(String variantId, ProductVariantDTO variantDTO) throws Exception;
    ProductVariantResponse getProductVariantById(String variantId) throws Exception;
    List<ProductVariantResponse> getProductVariantsByProduct(String productId);
    void deleteProductVariant(String variantId) throws Exception;
    
    // Stock Management
    ProductVariantResponse updateStock(String variantId, Integer newStock) throws Exception;
    ProductVariantResponse increaseStock(String variantId, Integer quantity) throws Exception;
    ProductVariantResponse decreaseStock(String variantId, Integer quantity) throws Exception;
    
    // Inventory Reports
    List<ProductVariantResponse> getLowStockItems(Integer threshold);
    List<ProductVariantResponse> getOutOfStockItems();
    List<ProductVariantResponse> getAllVariants();
}
