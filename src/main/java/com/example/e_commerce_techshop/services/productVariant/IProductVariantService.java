package com.example.e_commerce_techshop.services.productVariant;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.dtos.ProductVariantDTO;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IProductVariantService {
    void createProductVariant(ProductVariantDTO productVariantDTO, MultipartFile imageFile) throws Exception;

    void updateProductVariant(String productVariantId, ProductVariantDTO productVariantDTO, MultipartFile imageFile) throws Exception;

    void disableProduct(String productVariantId) throws Exception;

    List<ProductVariantResponse> getByProduct(String productId) throws Exception;

    List<ProductVariantResponse> getByCategory(String category) throws Exception;

    List<ProductVariantResponse> getByCategoryAndBrand(String category, String brand);

    List<ProductVariantResponse> filterProducts(ProductFilterDTO filterDTO);

}
