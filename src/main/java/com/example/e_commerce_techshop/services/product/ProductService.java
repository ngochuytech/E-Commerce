package com.example.e_commerce_techshop.services.product;

import com.example.e_commerce_techshop.dtos.ProductDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Brand;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.repositories.BrandRepository;
import com.example.e_commerce_techshop.repositories.ProductRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.responses.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService{
    private final ProductRepository productRepository;

    private final StoreRepository storeRepository;

    private final BrandRepository brandRepository;


    @Override
    public ProductResponse findProductById(String id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product này"));
        return ProductResponse.fromProduct(product);
    }

    @Override
    public List<ProductResponse> findProductByName(String name) {
        List<Product> productList = productRepository.findByNameContainingIgnoreCase(name);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }

    @Override
    public List<ProductResponse> findProductByCategory(String category) {
        List<Product> productList = productRepository.findByCategory(category);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }

    @Override
    public List<ProductResponse> findProductByCategoryAndBrand(String category, String brand) {
        List<Product> productList = productRepository.findByCategoryAndBrand_Name(category, brand);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }

    @Override
    public void createProduct(ProductDTO productDTO) throws Exception{
        Store store = storeRepository.findById(productDTO.getStoreId())
                .orElseThrow(() -> new DataNotFoundException("Cửa hàng không tồn tại"));
        Brand brand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new DataNotFoundException("Nhãn hiệu không tồn tại"));
        Product product = Product.builder()
                .name(productDTO.getName())
                .category(productDTO.getCategory())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .productCondition(productDTO.getProductCondition())
                .status(productDTO.getStatus())
                .brand(brand)
                .store(store)
                .build();
        productRepository.save(product);
    }

    @Override
    public void updateProduct(String productId, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product cần chỉnh sửa"));
        Brand newBrand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhãn hàng cần cập nhật"));
        existingProduct.setName(productDTO.getName());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setProductCondition(productDTO.getProductCondition());
        existingProduct.setStatus(productDTO.getStatus());
        existingProduct.setBrand(newBrand);
        // Ko update store
        productRepository.save(existingProduct);
    }

    @Override
    public void updateStatus(String productId, String status) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product cần cập nhật"));
        existingProduct.setStatus(status);
        productRepository.save(existingProduct);
    }
}
