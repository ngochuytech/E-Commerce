package com.example.e_commerce_techshop.services.product;

import com.example.e_commerce_techshop.dtos.ProductDTO;
import com.example.e_commerce_techshop.responses.ProductResponse;

import java.util.List;

public interface IProductService {
    ProductResponse findProductById(String id) throws Exception;

    List<ProductResponse> findProductByName(String name);

    List<ProductResponse> findProductByCategory(String category);

    List<ProductResponse> findProductByCategoryAndBrand(String category, String brand);

    void createProduct(ProductDTO productDTO) throws Exception;

    void updateProduct(String productId, ProductDTO productDTO);

    void updateStatus(String productId, String status);
}
