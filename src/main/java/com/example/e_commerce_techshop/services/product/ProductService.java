package com.example.e_commerce_techshop.services.product;

import com.example.e_commerce_techshop.dtos.ProductDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Brand;
import com.example.e_commerce_techshop.models.Category;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.repositories.BrandRepository;
import com.example.e_commerce_techshop.repositories.CategoryRepository;
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

    private final CategoryRepository categoryRepository;


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
        Category cate = categoryRepository.findByName(category).orElse(null);
        if (cate == null) {
            return List.of();
        }
        List<Product> productList = productRepository.findByCategoryId(cate.getId());
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }

    @Override
    public List<ProductResponse> findProductByCategoryAndBrand(String category, String brand) {
        // Tìm category và brand theo tên trước
        Category cate = categoryRepository.findByName(category).orElse(null);
        Brand brandObj = brandRepository.findByName(brand).orElse(null);
        
        if (cate == null || brandObj == null) {
            return List.of();
        }
        
        // Sử dụng ID của category và brand để tìm products
        List<Product> productList = productRepository.findByCategoryIdAndBrandId(cate.getId(), brandObj.getId());
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }

    @Override
    public void createProduct(ProductDTO productDTO) throws Exception{
        Store store = storeRepository.findById(productDTO.getStoreId())
                .orElseThrow(() -> new DataNotFoundException("Cửa hàng không tồn tại"));
        Brand brand = brandRepository.findByName(productDTO.getBrand())
                .orElseThrow(() -> new DataNotFoundException("Nhãn hiệu không tồn tại"));
        Category category = categoryRepository.findByName(productDTO.getCategory())
                .orElseThrow(() -> new DataNotFoundException("Danh mục không tồn tại"));
        Product product = Product.builder()
                .name(productDTO.getName())
                .category(category)
                .brand(brand)
                .store(store)   
                .build();
        productRepository.save(product);
    }

    @Override
    public void updateProduct(String productId, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product cần chỉnh sửa"));
        Category newCategory = categoryRepository.findByName(productDTO.getCategory())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy danh mục cần cập nhật"));
        Brand newBrand = brandRepository.findByName(productDTO.getBrand())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhãn hàng cần cập nhật"));
        existingProduct.setName(productDTO.getName());
        existingProduct.setCategory(newCategory);
        existingProduct.setDescription(productDTO.getDescription());
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
    
    // Thêm các methods hữu ích khác
    public List<ProductResponse> findProductByBrand(String brandName) {
        Brand brand = brandRepository.findByName(brandName).orElse(null);
        if (brand == null) {
            return List.of();
        }
        List<Product> productList = productRepository.findByBrandId(brand.getId());
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }
    
    public List<ProductResponse> findProductByStore(String storeId) {
        List<Product> productList = productRepository.findByStoreId(storeId);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }
    
    public List<ProductResponse> findProductByStatus(String status) {
        List<Product> productList = productRepository.findByStatus(status);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }
    
    public List<ProductResponse> findProductByCategoryAndStatus(String categoryName, String status) {
        Category category = categoryRepository.findByName(categoryName).orElse(null);
        if (category == null) {
            return List.of();
        }
        List<Product> productList = productRepository.findByCategoryIdAndStatus(category.getId(), status);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }
    
    public List<ProductResponse> findProductByBrandAndStatus(String brandName, String status) {
        Brand brand = brandRepository.findByName(brandName).orElse(null);
        if (brand == null) {
            return List.of();
        }
        List<Product> productList = productRepository.findByBrandIdAndStatus(brand.getId(), status);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }
    
    public List<ProductResponse> findProductByStoreAndStatus(String storeId, String status) {
        List<Product> productList = productRepository.findByStoreIdAndStatus(storeId, status);
        return productList.stream().map(ProductResponse::fromProduct).toList();
    }
}
