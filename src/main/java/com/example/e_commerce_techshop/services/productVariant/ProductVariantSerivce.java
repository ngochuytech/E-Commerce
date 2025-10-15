package com.example.e_commerce_techshop.services.productVariant;

import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ProductVariantDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductVariantSerivce implements IProductVariantService{

    private final ProductVariantRepository productVariantRepository;

    private final ProductRepository productRepository;

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
                .categoryName(product.getCategory().getName())
                .brandName(product.getBrand().getName())
                .storeId(product.getStore().getId())
                .price(productVariantDTO.getPrice())
                .description(productVariantDTO.getDescription())
                .stock(productVariantDTO.getStock())
                .attributes(productVariantDTO.getAttributes())
                .imageUrls(imageUrls)
                .primaryImageUrl(primaryImageUrl)
                .build();

        productVariantRepository.save(productVariant);
    }

    @Override
    public void createProductVariant(ProductVariantDTO productVariantDTO) throws Exception {
                Product product = productRepository.findById(productVariantDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product này"));

        ProductVariant productVariant = ProductVariant.builder()
                .product(product)
                .name(productVariantDTO.getName())
                .categoryName(product.getCategory().getName())
                .brandName(product.getBrand().getName())
                .storeId(product.getStore().getId())
                .price(productVariantDTO.getPrice())
                .description(productVariantDTO.getDescription())
                .stock(productVariantDTO.getStock())
                .attributes(productVariantDTO.getAttributes())
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
        
        List<ProductVariant> productVariants = productVariantRepository.findByProductId(productId);
        
        return productVariants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getByCategory(String category) throws Exception {
        List<ProductVariant> productVariants = productVariantRepository.findByCategoryName(category);

        return productVariants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getByCategoryAndBrand(String category, String brand) {
        List<ProductVariant> productVariant = productVariantRepository.findByCategoryNameAndBrandName(category, brand);
        return productVariant.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    @Transactional
    public void updateProductVariant(String productVariantId, ProductVariantDTO productVariantDTO, MultipartFile imageFile) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        // Cập nhật thông tin cơ bản
        if(productVariantDTO != null){
            productVariant.setName(productVariantDTO.getName());
            productVariant.setPrice(productVariantDTO.getPrice());
            productVariant.setDescription(productVariantDTO.getDescription());
            productVariant.setStock(productVariantDTO.getStock());
        }

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
        
        Page<ProductVariant> variantPage = productVariantRepository.findByStoreId(storeId, pageable);
        
        return variantPage.map(ProductVariantResponse::fromProductVariant);
    }
    

    @Override
    public void addProductVariantColors(String productVariantId, ColorOption colorOptionDTO, MultipartFile image) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        List<ProductVariant.ColorOption> colors = productVariant.getColors();
        if (colors == null) {
            colors = new ArrayList<>();
        }

        String image_url = fileUploadService.uploadFile(image, "product-variants");

        // Thêm màu mới
        ProductVariant.ColorOption newColor = ProductVariant.ColorOption.builder()
                .id(UUID.randomUUID().toString())
                .colorName(colorOptionDTO.getColorName())
                .price(colorOptionDTO.getPrice())
                .stock(colorOptionDTO.getStock())
                .image(image_url)
                .build();
        colors.add(newColor);
        productVariant.setColors(colors);

        productVariantRepository.save(productVariant);
    }

    @Override
    public void updateProductVariantColors(String productVariantId, String colorId, ColorOption colorOptionDTO, MultipartFile imageFile)
            throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        ProductVariant.ColorOption colorToUpdate = productVariant.getColors().stream()
                .filter(color -> color.getId().equals(colorId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy màu sắc với id được cung cấp"));
        colorToUpdate.setColorName(colorOptionDTO.getColorName());
        colorToUpdate.setPrice(colorOptionDTO.getPrice());
        colorToUpdate.setStock(colorOptionDTO.getStock());
        if(imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (colorToUpdate.getImage() != null && !colorToUpdate.getImage().isEmpty()) {
                fileUploadService.deleteFile(colorToUpdate.getImage());
            }
            // Upload ảnh mới
            String newImageUrl = fileUploadService.uploadFile(imageFile, "product-variants");
            colorToUpdate.setImage(newImageUrl);
        }
        productVariantRepository.save(productVariant);
    }
}