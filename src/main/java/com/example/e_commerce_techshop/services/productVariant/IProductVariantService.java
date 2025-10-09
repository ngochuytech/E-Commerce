package com.example.e_commerce_techshop.services.productVariant;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.dtos.ProductVariantDTO;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductVariantService {
    void createProductVariant(ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles) throws Exception;

    void updateProductVariant(String productVariantId, ProductVariantDTO productVariantDTO, MultipartFile imageFile) throws Exception;
    
    void updateProductVariantWithImages(String productVariantId, ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles) throws Exception;

    void disableProduct(String productVariantId) throws Exception;

    ProductVariantResponse getById(String productVariantId) throws Exception;

    List<ProductVariantResponse> getByProduct(String productId) throws Exception;

    List<ProductVariantResponse> getByCategory(String category) throws Exception;

    List<ProductVariantResponse> getByCategoryAndBrand(String category, String brand);

    List<ProductVariantResponse> filterProducts(ProductFilterDTO filterDTO);

    Page<ProductVariantResponse> getLatestProductVariants(int page, int size, String sortBy, String sortDir) throws Exception;

    Page<ProductVariantResponse> getByStore(String storeId, int page, int size, String sortBy, String sortDir) throws Exception;

}
