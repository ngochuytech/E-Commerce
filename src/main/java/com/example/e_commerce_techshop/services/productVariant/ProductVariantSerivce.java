package com.example.e_commerce_techshop.services.productVariant;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.dtos.ProductVariantDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Category;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.repositories.*;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantSerivce implements IProductVariantService{

    private final ProductVariantRepository productVariantRepository;

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final FileUploadService fileUploadService;

    @Override
    @Transactional
    public void createProductVariant(ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles) throws Exception {
        Product product = productRepository.findById(productVariantDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product này"));

        // Xử lý upload ảnh
        List<String> imageUrls = new ArrayList<>();
        String primaryImageUrl = null;
        
        if (imageFiles != null && !imageFiles.isEmpty()) {
            imageUrls = fileUploadService.uploadFiles(imageFiles, "product-variants");
            primaryImageUrl = imageUrls.get(0); // Ảnh đầu tiên là ảnh chính
        }

        ProductVariant productVariant = ProductVariant.builder()
                .product(product)
                .name(productVariantDTO.getName())
                .price(productVariantDTO.getPrice())
                .description(productVariantDTO.getDescription())
                .stock(productVariantDTO.getStock())
                .attributes(productVariantDTO.getAttributes()) // Lưu trực tiếp Map
                .imageUrls(imageUrls) // Lưu trực tiếp List<String>
                .primaryImageUrl(primaryImageUrl)
                .build();

        productVariantRepository.save(productVariant);
    }

    @Override
    public void disableProduct(String productVariantId) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        productVariant.setStock(0);
        productVariantRepository.save(productVariant);
    }

    @Override
    public ProductVariantResponse getById(String productVariantId) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        return ProductVariantResponse.fromProductVariant(productVariant);
    }

    @Override
    public List<ProductVariantResponse> getByProduct(String productId) throws Exception {
        if(!productRepository.existsById(productId))
            throw new DataNotFoundException("Không tìm thấy sản phẩm với id được cung cấp");
        
        // Debug: In ra productId để kiểm tra
        System.out.println("Searching for ProductVariants with productId: " + productId);
        
        // Thử method đầu tiên
        List<ProductVariant> productVariants = productVariantRepository.findByProductId(productId);
        System.out.println("Method 1 - findByProductId: Found " + productVariants.size() + " results");
        
        // Nếu không tìm được, thử method thứ 2
        if (productVariants.isEmpty()) {
            productVariants = productVariantRepository.findByProductIdWithQuery(productId);
            System.out.println("Method 2 - findByProductIdWithQuery: Found " + productVariants.size() + " results");
        }
        
        // Nếu vẫn không tìm được, thử method thứ 3
        if (productVariants.isEmpty()) {
            productVariants = productVariantRepository.findByProductIdWithObjectId(productId);
            System.out.println("Method 3 - findByProductIdWithObjectId: Found " + productVariants.size() + " results");
        }
        
        // Debug: In ra thông tin của từng ProductVariant
        for (ProductVariant pv : productVariants) {
            System.out.println("ProductVariant ID: " + pv.getId() + 
                             ", Name: " + pv.getName() + 
                             ", Product ID: " + (pv.getProduct() != null ? pv.getProduct().getId() : "null"));
        }
        
        return productVariants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getByCategory(String category) throws Exception {
        Category cate = categoryRepository.findByName(category).orElse(null);
        if (cate == null) {
            return List.of();
        }
        List<ProductVariant> productVariants = productVariantRepository.findByProductCategoryId(cate.getId());
        
        return productVariants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getByCategoryAndBrand(String category, String brand) {
        List<ProductVariant> productVariant = productVariantRepository.findByProductCategoryAndProductBrandName(category, brand);
        return productVariant.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> filterProducts(ProductFilterDTO filterDTO) {
        // Tạm thời return empty list vì productRepositoryCustom chưa được implement cho MongoDB
        // Cần implement MongoDB aggregation pipeline cho complex filtering
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public void updateProductVariant(String productVariantId, ProductVariantDTO productVariantDTO, MultipartFile imageFile) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        
        // Cập nhật thông tin cơ bản
        productVariant.setName(productVariantDTO.getName());
        productVariant.setPrice(productVariantDTO.getPrice());
        productVariant.setDescription(productVariantDTO.getDescription());
        productVariant.setStock(productVariantDTO.getStock());

        // Xử lý ảnh đơn
        if (imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (productVariant.getImageUrls() != null && !productVariant.getImageUrls().isEmpty()) {
                for (String oldImageUrl : productVariant.getImageUrls()) {
                    fileUploadService.deleteFile(oldImageUrl);
                }
            }

            // Upload ảnh mới
            String newImageUrl = fileUploadService.uploadFile(imageFile, "product-variants");
            if (newImageUrl != null) {
                List<String> newImageUrls = new ArrayList<>();
                newImageUrls.add(newImageUrl);
                productVariant.setImageUrls(newImageUrls);
                productVariant.setPrimaryImageUrl(newImageUrl);
            }
        }

        // Cập nhật attributes
        if (productVariantDTO.getAttributes() != null) {
            productVariant.setAttributes(productVariantDTO.getAttributes());
        }

        productVariantRepository.save(productVariant);
    }

    @Override
    @Transactional
    public void updateProductVariantWithImages(String productVariantId, ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        
        // Cập nhật thông tin cơ bản
        productVariant.setName(productVariantDTO.getName());
        productVariant.setPrice(productVariantDTO.getPrice());
        productVariant.setDescription(productVariantDTO.getDescription());
        productVariant.setStock(productVariantDTO.getStock());

        // Xử lý nhiều ảnh
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (productVariant.getImageUrls() != null && !productVariant.getImageUrls().isEmpty()) {
                for (String oldImageUrl : productVariant.getImageUrls()) {
                    fileUploadService.deleteFile(oldImageUrl);
                }
            }

            // Upload ảnh mới
            List<String> newImageUrls = fileUploadService.uploadFiles(imageFiles, "product-variants");
            productVariant.setImageUrls(newImageUrls);
            if (!newImageUrls.isEmpty()) {
                productVariant.setPrimaryImageUrl(newImageUrls.get(0)); // Ảnh đầu tiên là ảnh chính
            }
        }

        // Cập nhật attributes
        if (productVariantDTO.getAttributes() != null) {
            productVariant.setAttributes(productVariantDTO.getAttributes());
        }

        productVariantRepository.save(productVariant);
    }

    @Override
    public Page<ProductVariantResponse> getLatestProductVariants(int page, int size, String sortBy, String sortDir) throws Exception {
        Pageable pageable = PageRequest.of(page, size, 
            sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
        
        Page<ProductVariant> variantPage = productVariantRepository.findAll(pageable);
        
        return variantPage.map(ProductVariantResponse::fromProductVariant);
    }

    @Override
    public Page<ProductVariantResponse> getByStore(String storeId, int page, int size, String sortBy, String sortDir) throws Exception {
        Pageable pageable = PageRequest.of(page, size, 
            sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());
        
        Page<ProductVariant> variantPage = productVariantRepository.findByProductStoreId(storeId, pageable);
        
        return variantPage.map(ProductVariantResponse::fromProductVariant);
    }
    
    // Debug method để kiểm tra tất cả ProductVariants
    public void debugAllProductVariants() {
        List<ProductVariant> allVariants = productVariantRepository.findAll();
        System.out.println("=== ALL PRODUCT VARIANTS IN DATABASE ===");
        System.out.println("Total count: " + allVariants.size());
        
        for (ProductVariant pv : allVariants) {
            System.out.println("ProductVariant ID: " + pv.getId());
            System.out.println("  - Name: " + pv.getName());
            System.out.println("  - Product: " + (pv.getProduct() != null ? pv.getProduct().getId() : "null"));
            System.out.println("  - Product Name: " + (pv.getProduct() != null ? pv.getProduct().getName() : "null"));
            System.out.println("  - Category: " + (pv.getProduct() != null && pv.getProduct().getCategory() != null ? 
                                                  pv.getProduct().getCategory().getName() : "null"));
            System.out.println("  - Category ID: " + (pv.getProduct() != null && pv.getProduct().getCategory() != null ? 
                                                     pv.getProduct().getCategory().getId() : "null"));
            System.out.println("---");
        }
    }
    
    // Debug method để kiểm tra tất cả Categories
    public void debugAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        System.out.println("=== ALL CATEGORIES IN DATABASE ===");
        System.out.println("Total count: " + allCategories.size());
        
        for (Category cat : allCategories) {
            System.out.println("Category ID: " + cat.getId());
            System.out.println("  - Name: " + cat.getName());
            System.out.println("  - Description: " + cat.getDescription());
            System.out.println("---");
        }
    }
}