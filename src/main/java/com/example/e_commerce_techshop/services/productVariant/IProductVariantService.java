package com.example.e_commerce_techshop.services.productVariant;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ProductVariantDTO;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;

public interface IProductVariantService {

    ProductVariant getProductVariantById(String productVariantId) throws Exception;

    ProductVariantResponse getById(String productVariantId) throws Exception;

    Page<ProductVariantResponse> getByProduct(String productId, Pageable pageable) throws Exception;

    Page<ProductVariantResponse> getByCategory(String category, Pageable pageable) throws Exception;

    Page<ProductVariantResponse> getByCategoryAndBrand(String category, String brand, Pageable pageable);

    Page<ProductVariantResponse> getLatestProductVariants(Pageable pageable) throws Exception;

    Page<ProductVariantResponse> getByStore(String storeId, Pageable pageable) throws Exception;

    Page<ProductVariantResponse> searchByName(String name, int page, int size, String sortBy, String sortDir) throws Exception;

    // Shop (B2C) methods
    Page<ProductVariant> getAllProductVariantsB2C(String storeId, String status, Pageable pageable) throws Exception;

    Page<ProductVariant> searchByStoreAndName(String storeId, String name, String status, Pageable pageable) throws Exception;

    Map<String, Long> countProductVariantsByStatus(String storeId) throws Exception;

    void createProductVariant(ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles, String primaryImageIndex) throws Exception;

    void createProductVariant(ProductVariantDTO productVariantDTO) throws Exception;

    void addProductVariantColors(String productVariantId, ColorOption colorOptionDTO, MultipartFile imageFile) throws Exception;

    void updateProductVariantColors(String productVariantId, String colorId, ColorOption colorOptionDTO, MultipartFile imageFile) throws Exception;

    void removeProductVariantColor(String productVariantId, String colorId) throws Exception;

    void updateProductVariantImages(String productVariantId, List<MultipartFile> imageFiles, int indexPrimary) throws Exception;

    void updateStock(String productVariantId, int newStock) throws Exception;

    void updatePrice(String productVariantId, Long newPrice) throws Exception;

    void updatePriceAndStock(String productVariantId, Long newPrice, int newStock) throws Exception;

    void disableProduct(String productVariantId) throws Exception;

    // Admin methods
    Page<ProductVariantResponse> getVariantsByStatus(String status, Pageable pageable);

    void updateVariantStatus(String variantId, String status) throws Exception;

    void rejectVariant(String variantId, String reason) throws Exception;

}
