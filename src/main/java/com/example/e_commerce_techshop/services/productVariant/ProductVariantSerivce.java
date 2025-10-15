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
        productVariant.setPrice(0L);
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
    @Transactional
    public void updateProductVariant(String productVariantId, ProductVariantDTO productVariantDTO, MultipartFile imageFile) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        // Cập nhật thông tin cơ bản
        if(productVariantDTO != null){
            productVariant.setName(productVariantDTO.getName());
            productVariant.setDescription(productVariantDTO.getDescription());
            
            // Chỉ cập nhật price và stock nếu không có màu sắc (nếu có màu sắc thì được tính tự động)
            if (productVariant.getColors() == null || productVariant.getColors().isEmpty()) {
                productVariant.setPrice(productVariantDTO.getPrice());
                productVariant.setStock(productVariantDTO.getStock());
            }

            // Cập nhật attributes
            if (productVariantDTO.getAttributes() != null) {
                productVariant.setAttributes(productVariantDTO.getAttributes());
            }
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

        productVariantRepository.save(productVariant);
    }

    @Override
    @Transactional
    public void updateProductVariantWithImages(String productVariantId, ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        // Cập nhật thông tin cơ bản
        if(productVariantDTO != null){
            productVariant.setName(productVariantDTO.getName());
            productVariant.setDescription(productVariantDTO.getDescription());
            
            // Chỉ cập nhật price và stock nếu không có màu sắc (nếu có màu sắc thì được tính tự động)
            if (productVariant.getColors() == null || productVariant.getColors().isEmpty()) {
                productVariant.setPrice(productVariantDTO.getPrice());
                productVariant.setStock(productVariantDTO.getStock());
            }

            // Cập nhật attributes
            if (productVariantDTO.getAttributes() != null) {
                productVariant.setAttributes(productVariantDTO.getAttributes());
            }

        }

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
        
        productVariantRepository.save(productVariant);
    }

    @Override
    @Transactional
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

        // Cập nhật tổng stock = tổng stock của tất cả màu
        int totalStock = colors.stream().mapToInt(ProductVariant.ColorOption::getStock).sum();
        productVariant.setStock(totalStock);

        // Cập nhật price = giá thấp nhất của tất cả màu
        Long minPrice = colors.stream().mapToLong(ProductVariant.ColorOption::getPrice).min().orElse(0L);
        productVariant.setPrice(minPrice);

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
        if(colorOptionDTO != null) {
            colorToUpdate.setColorName(colorOptionDTO.getColorName());
            colorToUpdate.setPrice(colorOptionDTO.getPrice());
            colorToUpdate.setStock(colorOptionDTO.getStock());
        }
        if(imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (colorToUpdate.getImage() != null && !colorToUpdate.getImage().isEmpty()) {
                fileUploadService.deleteFile(colorToUpdate.getImage());
            }
            // Upload ảnh mới
            String newImageUrl = fileUploadService.uploadFile(imageFile, "product-variants");
            colorToUpdate.setImage(newImageUrl);
        }

        // Cập nhật tổng stock = tổng stock của tất cả màu
        int totalStock = productVariant.getColors().stream().mapToInt(ProductVariant.ColorOption::getStock).sum();
        productVariant.setStock(totalStock);

        // Cập nhật price = giá thấp nhất của tất cả màu
        Long minPrice = productVariant.getColors().stream().mapToLong(ProductVariant.ColorOption::getPrice).min().orElse(0L);
        productVariant.setPrice(minPrice);

        productVariantRepository.save(productVariant);
    }

    @Override
    public void updateStock(String productVariantId, int newStock) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        
        // Chỉ cho phép cập nhật stock trực tiếp nếu không có màu sắc
        if (productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            throw new IllegalArgumentException("Không thể cập nhật stock trực tiếp cho sản phẩm có màu sắc. Hãy cập nhật stock của từng màu.");
        }
        
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock không thể âm");
        }
        
        productVariant.setStock(newStock);
        productVariantRepository.save(productVariant);
    }

    /**
     * Cập nhật price và stock cho ProductVariant không có màu sắc
     */
    public void updatePriceAndStock(String productVariantId, Long newPrice, int newStock) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        
        // Chỉ cho phép cập nhật trực tiếp nếu không có màu sắc
        if (productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            throw new IllegalArgumentException("Không thể cập nhật price/stock trực tiếp cho sản phẩm có màu sắc. Hãy cập nhật của từng màu.");
        }
        
        if (newPrice != null && newPrice < 0) {
            throw new IllegalArgumentException("Price không thể âm");
        }
        
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock không thể âm");
        }
        
        if (newPrice != null) {
            productVariant.setPrice(newPrice);
        }
        productVariant.setStock(newStock);
        productVariantRepository.save(productVariant);
    }

    /**
     * Xóa màu sắc của product variant và cập nhật tổng stock
     */
    @Override
    public void removeProductVariantColor(String productVariantId, String colorId) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        
        List<ProductVariant.ColorOption> colors = productVariant.getColors();
        if (colors == null || colors.isEmpty()) {
            throw new DataNotFoundException("Sản phẩm không có màu sắc nào");
        }

        // Tìm và xóa màu
        ProductVariant.ColorOption colorToRemove = colors.stream()
                .filter(color -> color.getId().equals(colorId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy màu sắc với id được cung cấp"));

        // Xóa ảnh nếu có
        if (colorToRemove.getImage() != null && !colorToRemove.getImage().isEmpty()) {
            fileUploadService.deleteFile(colorToRemove.getImage());
        }

        // Xóa màu khỏi danh sách
        colors.remove(colorToRemove);
        productVariant.setColors(colors);

        // Cập nhật tổng stock = tổng stock của tất cả màu còn lại
        int totalStock = colors.stream().mapToInt(ProductVariant.ColorOption::getStock).sum();
        productVariant.setStock(totalStock);

        // Cập nhật price = giá thấp nhất của tất cả màu còn lại
        if (!colors.isEmpty()) {
            Long minPrice = colors.stream().mapToLong(ProductVariant.ColorOption::getPrice).min().orElse(0L);
            productVariant.setPrice(minPrice);
        } else {
            // Không còn màu nào -> giữ nguyên price hiện tại (không set về 0)
            // User có thể tự cập nhật price thông qua updateProductVariant
        }

        productVariantRepository.save(productVariant);
    }

    /**
     * Tính toán lại tổng stock và price từ tất cả màu sắc
     */
    public void recalculateStock(String productVariantId) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        if (productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            // Có màu sắc -> tính tổng stock và price từ các màu
            int totalStock = productVariant.getColors().stream().mapToInt(ProductVariant.ColorOption::getStock).sum();
            productVariant.setStock(totalStock);
            
            // Cập nhật price = giá thấp nhất của tất cả màu (chỉ khi có màu sắc)
            Long minPrice = productVariant.getColors().stream().mapToLong(ProductVariant.ColorOption::getPrice).min().orElse(0L);
            productVariant.setPrice(minPrice);
        } else {
            // Không có màu sắc -> stock và price hiện tại không thay đổi (do user thiết lập thủ công)
            // Không cập nhật gì
        }

        productVariantRepository.save(productVariant);
    }


}