package com.example.e_commerce_techshop.services.product;

import com.example.e_commerce_techshop.dtos.ProductDTO;
import com.example.e_commerce_techshop.responses.ProductResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    ProductResponse findProductById(String id) throws Exception;

    Page<ProductResponse> findProductByName(String name, Pageable pageable);

    Page<ProductResponse> findProductByCategory(String category, Pageable pageable);

    Page<ProductResponse> findProductByCategoryAndBrand(String category, String brand, Pageable pageable);

    void createProduct(ProductDTO productDTO) throws Exception;

    void updateProduct(String productId, ProductDTO productDTO);

    void updateStatus(String productId, String status);

    // Admin methods
    Page<ProductResponse> getPendingProducts(Pageable pageable);
    void rejectProduct(String productId, String reason) throws Exception;
}
