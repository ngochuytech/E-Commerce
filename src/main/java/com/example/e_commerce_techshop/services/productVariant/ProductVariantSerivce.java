package com.example.e_commerce_techshop.services.productVariant;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.dtos.ProductVariantDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Attribute;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.ProductImage;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ProductVariantAttribute;
import com.example.e_commerce_techshop.repositories.*;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductVariantSerivce implements IProductVariantService{

    private final ProductVariantRepository productVariantRepository;

    private final ProductRepository productRepository;

    private final AttributeRepository attributeRepository;

    private final ProductVariantAttributeRepository productAttributeRepository;

    private final ProductRepositoryCustom productRepositoryCustom;

    private final FileUploadService fileUploadService;
    
    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public void createProductVariant(ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles) throws Exception {
        Product product = productRepository.findById(productVariantDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product này"));
        ProductVariant productVariant = ProductVariant.builder()
                .product(product)
                .name(productVariantDTO.getName())
                .price(productVariantDTO.getPrice())
                .description(productVariantDTO.getDescription())
                .stock(productVariantDTO.getStock())
                .build();

        // Lưu product variant trước
        productVariantRepository.save(productVariant);

        // Xử lý nhiều ảnh
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> imageUrls = fileUploadService.uploadFiles(imageFiles, "product-variants");
            
            for (int i = 0; i < imageUrls.size(); i++) {
                String imageUrl = imageUrls.get(i);
                MultipartFile file = imageFiles.get(i);
                
                ProductImage productImage = ProductImage.builder()
                        .productVariant(productVariant)
                        .mediaPath(imageUrl)
                        .mediaType(file.getContentType())
                        .isPrimary(i == 0) // Ảnh đầu tiên là ảnh chính
                        .build();
                
                productImageRepository.save(productImage);
            }
        }

        // Xử lý lưu các attributes
        Map<String, String> attributeMaps = productVariantDTO.getAttributes();
        if(attributeMaps != null){
            for(Map.Entry<String, String> entry : attributeMaps.entrySet()){
                String attrName = entry.getKey();
                String value = entry.getValue();

                // Tạo attribute nếu chưa tồn tại
                Attribute attribute = attributeRepository.findByName(attrName)
                        .orElseGet(() -> {
                            Attribute newAttr = new Attribute();
                            newAttr.setName(attrName);
                            return attributeRepository.save(newAttr);
                        });
                ProductVariantAttribute productVariantAttribute = ProductVariantAttribute.builder()
                        .productVariant(productVariant)
                        .attribute(attribute)
                        .value(value)
                        .build();
                productAttributeRepository.save(productVariantAttribute);
            }
        }
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
        List<ProductVariant> productVariant = productVariantRepository.findByProductId(productId);
        return productVariant.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getByCategory(String category) throws Exception {
        List<ProductVariant> productVariant = productVariantRepository.findByProduct_Category(category);
        return productVariant.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> getByCategoryAndBrand(String category, String brand) {
        List<ProductVariant> productVariant = productVariantRepository.findByProduct_CategoryAndProduct_Brand_Name(category, brand);
        return productVariant.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    public List<ProductVariantResponse> filterProducts(ProductFilterDTO filterDTO) {
        List<ProductVariant> productVariants = productRepositoryCustom.findProductsByFilters(filterDTO);
        return productVariants.stream().map(ProductVariantResponse::fromProductVariant).toList();
    }

    @Override
    @Transactional
    public void updateProductVariant(String productVariantId, ProductVariantDTO productVariantDTO, MultipartFile imageFile) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        productVariant.setName(productVariantDTO.getName());
        productVariant.setPrice(productVariantDTO.getPrice());
        productVariant.setDescription(productVariantDTO.getDescription());
        productVariant.setStock(productVariantDTO.getStock());

        // Xử lý ảnh (tạm thời giữ logic cũ cho updateProductVariant với 1 ảnh)
        if (imageFile != null && !imageFile.isEmpty()) {
            // Xóa tất cả ảnh cũ
            List<ProductImage> existingImages = productImageRepository.findByProductVariant(productVariant);
            for (ProductImage img : existingImages) {
                fileUploadService.deleteFile(img.getMediaPath());
            }
            productImageRepository.deleteByProductVariant(productVariant);

            // Lưu ảnh mới
            String newImageUrl = fileUploadService.uploadFile(imageFile, "product-variants");
            if (newImageUrl != null) {
                ProductImage productImage = ProductImage.builder()
                        .productVariant(productVariant)
                        .mediaPath(newImageUrl)
                        .mediaType(imageFile.getContentType())
                        .isPrimary(true)
                        .build();
                productImageRepository.save(productImage);
            }
        }

        // Xử lý attributes
        updateAttributes(productVariant, productVariantDTO.getAttributes());

        productVariantRepository.save(productVariant);
    }

    @Override
    @Transactional
    public void updateProductVariantWithImages(String productVariantId, ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        
        productVariant.setName(productVariantDTO.getName());
        productVariant.setPrice(productVariantDTO.getPrice());
        productVariant.setDescription(productVariantDTO.getDescription());
        productVariant.setStock(productVariantDTO.getStock());

        // Xử lý nhiều ảnh
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // Xóa tất cả ảnh cũ
            List<ProductImage> existingImages = productImageRepository.findByProductVariant(productVariant);
            for (ProductImage img : existingImages) {
                fileUploadService.deleteFile(img.getMediaPath());
            }
            productImageRepository.deleteByProductVariant(productVariant);

            // Lưu các ảnh mới
            List<String> imageUrls = fileUploadService.uploadFiles(imageFiles, "product-variants");
            
            for (int i = 0; i < imageUrls.size(); i++) {
                String imageUrl = imageUrls.get(i);
                MultipartFile file = imageFiles.get(i);
                
                ProductImage productImage = ProductImage.builder()
                        .productVariant(productVariant)
                        .mediaPath(imageUrl)
                        .mediaType(file.getContentType())
                        .isPrimary(i == 0) // Ảnh đầu tiên là ảnh chính
                        .build();
                
                productImageRepository.save(productImage);
            }
        }

        // Xử lý attributes
        updateAttributes(productVariant, productVariantDTO.getAttributes());

        productVariantRepository.save(productVariant);
    }

    private void updateAttributes(ProductVariant variant, Map<String, String> newAttributes) {
        // Lấy danh sách attributes hiện tại
        List<ProductVariantAttribute> existingAttributes = productAttributeRepository.findByProductVariant(variant);

        // Tạo map để so sánh
        Map<String, String> newAttrMap = new HashMap<>();
        if (newAttributes != null) {
            newAttributes.forEach((key, values) -> {
                newAttrMap.put(key, values.isEmpty() ? "" : values);
            });
        }

        // Xóa attributes không còn trong DTO
        List<ProductVariantAttribute> toRemove = existingAttributes.stream()
                .filter(attr -> !newAttrMap.containsKey(attr.getAttribute().getName()))
                .toList();
        productAttributeRepository.deleteAll(toRemove);

        // Cập nhật hoặc thêm attributes
        for (Map.Entry<String, String> entry : newAttrMap.entrySet()) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();

            // Tìm hoặc tạo Attribute
            Attribute attribute = attributeRepository.findByName(attrName)
                    .orElseGet(() -> {
                        Attribute newAttr = new Attribute();
                        newAttr.setName(attrName);
                        return attributeRepository.save(newAttr);
                    });

            // Kiểm tra attribute hiện có
            Optional<ProductVariantAttribute> existingAttr = existingAttributes.stream()
                    .filter(attr -> attr.getAttribute().getName().equals(attrName))
                    .findFirst();

            if (existingAttr.isPresent()) {
                // Cập nhật giá trị nếu khác
                ProductVariantAttribute attr = existingAttr.get();
                if (!attr.getValue().equals(attrValue)) {
                    attr.setValue(attrValue);
                    productAttributeRepository.save(attr);
                }
            } else {
                // Thêm mới attribute
                ProductVariantAttribute newAttr = ProductVariantAttribute.builder()
                        .productVariant(variant)
                        .attribute(attribute)
                        .value(attrValue)
                        .build();
                productAttributeRepository.save(newAttr);
            }
        }
    }
}