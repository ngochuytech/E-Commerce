package com.example.e_commerce_techshop.services.productVariant;

import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.dtos.b2c.ProductVariant.ProductVariantDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.repositories.*;
import com.example.e_commerce_techshop.responses.ProductVariantResponse;
import com.example.e_commerce_techshop.services.FileUploadService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantSerivce implements IProductVariantService {

    private final ProductVariantRepository productVariantRepository;

    private final ProductRepository productRepository;

    private final FileUploadService fileUploadService;

    private final StoreRepository storeRepository;

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "product-variants:";
    private static final long CACHE_TTL = 1; // 1 hour

    @Override
    @Transactional
    public void createProductVariant(ProductVariantDTO productVariantDTO, List<MultipartFile> imageFiles,
            String primaryImageIndex) throws Exception {
        Product product = productRepository.findById(productVariantDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product này"));

        if (!Product.ProductStatus.APPROVED.name().equals(product.getStatus())) {
            throw new IllegalStateException("Sản phẩm chưa được duyệt, không thể thêm biến thể");
        }

        // Xử lý upload ảnh
        List<String> imageUrls = new ArrayList<>();
        String primaryImageUrl = null;

        if (imageFiles != null && !imageFiles.isEmpty()) {
            imageUrls = fileUploadService.uploadFiles(imageFiles, "product-variants");
            
            // Convert String sang Integer
            if (primaryImageIndex != null && !primaryImageIndex.trim().isEmpty()) {
                try {
                    int index = Integer.parseInt(primaryImageIndex);
                    if (index >= 0 && index < imageUrls.size()) {
                        primaryImageUrl = imageUrls.get(index);
                    } else {
                        throw new IllegalArgumentException(
                                "Chỉ số ảnh chính không hợp lệ. Phải từ 0 đến " + (imageUrls.size() - 1));
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Chỉ số ảnh chính phải là số nguyên");
                }
            } else {
                throw new IllegalArgumentException(
                        "Chỉ số ảnh chính không hợp lệ. Phải từ 0 đến " + (imageUrls.size() - 1));
            }
        }

        ProductVariant productVariant = ProductVariant.builder()
                .product(product)
                .name(productVariantDTO.getName())
                .categoryName(product.getCategory().getName())
                .brandName(product.getBrand().getName())
                .storeId(product.getStore().getId())
                .price(productVariantDTO.getPrice())
                .description(productVariantDTO.getDescription())
                .stock(0)
                .status(ProductVariant.VariantStatus.PENDING.name())
                .attributes(productVariantDTO.getAttributes())
                .imageUrls(imageUrls)
                .primaryImageUrl(primaryImageUrl)
                .build();

        invalidateProductVariantCache(productVariant);

        productVariantRepository.save(productVariant);
    }

    @Override
    public void createProductVariant(ProductVariantDTO productVariantDTO) throws Exception {
        Product product = productRepository.findById(productVariantDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy product này"));

        if (!Product.ProductStatus.APPROVED.name().equals(product.getStatus())) {
            throw new IllegalStateException("Sản phẩm chưa được duyệt, không thể thêm biến thể");
        }

        ProductVariant productVariant = ProductVariant.builder()
                .product(product)
                .name(productVariantDTO.getName())
                .categoryName(product.getCategory().getName())
                .brandName(product.getBrand().getName())
                .storeId(product.getStore().getId())
                .price(productVariantDTO.getPrice())
                .description(productVariantDTO.getDescription())
                .stock(0)
                .status(ProductVariant.VariantStatus.PENDING.name())
                .attributes(productVariantDTO.getAttributes())
                .build();

        invalidateProductVariantCache(productVariant);

        productVariantRepository.save(productVariant);
    }

    @Override
    public void disableProduct(String productVariantId) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        productVariant.setStatus(ProductVariant.VariantStatus.DELETED.name());
        invalidateProductVariantCache(productVariant);
        productVariantRepository.save(productVariant);
    }

    @Override
    public ProductVariantResponse getById(String productVariantId) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        if(productVariant.getProduct().getStore().getStatus().equals(Store.StoreStatus.BANNED.name())) {
            throw new IllegalStateException("Cửa hàng của sản phẩm này đã bị khóa, không thể xem sản phẩm");
        }

        if (!ProductVariant.VariantStatus.APPROVED.name().equals(productVariant.getStatus())) {
            throw new DataNotFoundException("Biến thể không khả dụng");
        }
        return ProductVariantResponse.fromProductVariant(productVariant);
    }

    @Override
    public Page<ProductVariantResponse> getByProduct(String productId, Pageable pageable) throws Exception {
        if (!productRepository.existsById(productId))
            throw new DataNotFoundException("Không tìm thấy sản phẩm với id được cung cấp");

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm với id được cung cấp"));
        
        if(product.getStore().getStatus().equals(Store.StoreStatus.BANNED.name())) {
            throw new IllegalStateException("Cửa hàng của sản phẩm này đã bị khóa, không thể xem sản phẩm");
        }

        Page<ProductVariant> productVariants = productVariantRepository.findByProductIdAndStatus(productId,
                ProductVariant.VariantStatus.APPROVED.name(), pageable);

        return productVariants.map(ProductVariantResponse::fromProductVariant);
    }

    @Override
    public Page<ProductVariantResponse> getByCategory(String category, Pageable pageable) throws Exception {
        String hashKey = CACHE_PREFIX + "category:" + category;
        String field = buildField(pageable);

        Object cached = redisTemplate.opsForHash().get(hashKey, field);

        if (cached != null) {
            try {
                PageData pageData = objectMapper.readValue(cached.toString(), PageData.class);
                return new PageImpl<>(pageData.getContent(), pageable, pageData.getTotalElements());
            } catch (Exception e) {
                redisTemplate.opsForHash().delete(hashKey, field);
            }
        }
        // Query database
        Page<ProductVariantResponse> productVariants = productVariantRepository
                .findByCategoryNameAndStatusExcludingBannedStores(category, ProductVariant.VariantStatus.APPROVED.name(), pageable)
                .map(ProductVariantResponse::fromProductVariant);

        try {
            PageData pageData = new PageData(productVariants.getContent(), productVariants.getTotalElements());
            String serialized = objectMapper.writeValueAsString(pageData);
            redisTemplate.opsForHash().put(hashKey, field, serialized);
            redisTemplate.expire(hashKey, CACHE_TTL, TimeUnit.HOURS);
        } catch (Exception e) {
            System.err.println("Error caching product variants by category: " + e.getMessage());
        }

        return productVariants;
    }

    @Override
    public Page<ProductVariantResponse> getByCategoryAndBrand(String category, String brand, Pageable pageable) {
        String hashKey = CACHE_PREFIX + "category:" + category + ":brand:" + brand;
        String field = buildField(pageable);

        Object cached = redisTemplate.opsForHash().get(hashKey, field);

        if (cached != null) {
            try {
                PageData pageData = objectMapper.readValue(cached.toString(), PageData.class);
                return new PageImpl<>(pageData.getContent(), pageable, pageData.getTotalElements());
            } catch (Exception e) {
                redisTemplate.opsForHash().delete(hashKey, field);
            }
        }

        Page<ProductVariant> productVariants = productVariantRepository
                .findByCategoryNameAndBrandNameAndStatusExcludingBannedStores(category, brand,
                        ProductVariant.VariantStatus.APPROVED.name(), pageable);

        Page<ProductVariantResponse> result = productVariants.map(ProductVariantResponse::fromProductVariant);

        try {
            PageData pageData = new PageData(result.getContent(), result.getTotalElements());
            redisTemplate.opsForHash().put(hashKey, field, objectMapper.writeValueAsString(pageData));
            redisTemplate.expire(hashKey, CACHE_TTL, TimeUnit.HOURS);
        } catch (Exception e) {
            System.err.println("Error caching product variants by category and brand: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Page<ProductVariantResponse> getLatestProductVariants(Pageable pageable)
            throws Exception {
        String hashKey = CACHE_PREFIX + "latest";
        String field = buildField(pageable);

        Object cached = redisTemplate.opsForHash().get(hashKey, field);

        if (cached != null) {
            try {
                PageData pageData = objectMapper.readValue(cached.toString(), PageData.class);
                return new PageImpl<>(pageData.getContent(), pageable, pageData.getTotalElements());
            } catch (Exception e) {
                redisTemplate.opsForHash().delete(hashKey, field);
            }
        }

        Page<ProductVariantResponse> result = productVariantRepository
                .findByStatusExcludingBannedStores(ProductVariant.VariantStatus.APPROVED.name(), pageable)
                .map(ProductVariantResponse::fromProductVariant);

        try {
            PageData pageData = new PageData(result.getContent(), result.getTotalElements());
            redisTemplate.opsForHash().put(hashKey, field, objectMapper.writeValueAsString(pageData));
            redisTemplate.expire(hashKey, CACHE_TTL, TimeUnit.HOURS);
        } catch (Exception e) {
            System.err.println("Error caching latest product variants: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Page<ProductVariantResponse> getByStore(String storeId, Pageable pageable)
            throws Exception {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng với id được cung cấp"));

        if(store.getStatus().equals(Store.StoreStatus.BANNED.name())) {
            throw new IllegalStateException("Cửa hàng đã bị khóa, không thể xem sản phẩm");
        }
            
        String hashKey = CACHE_PREFIX + "store:" + storeId;
        String field = buildField(pageable);

        Object cached = redisTemplate.opsForHash().get(hashKey, field);

        if (cached != null) {
            try {
                PageData pageData = objectMapper.readValue(cached.toString(), PageData.class);
                return new PageImpl<>(pageData.getContent(), pageable, pageData.getTotalElements());
            } catch (Exception e) {
                redisTemplate.opsForHash().delete(hashKey, field);
            }
        }

        Page<ProductVariantResponse> result = productVariantRepository
                .findByStoreIdAndStatus(storeId, ProductVariant.VariantStatus.APPROVED.name(), pageable)
                .map(ProductVariantResponse::fromProductVariant);

        try {
            PageData pageData = new PageData(result.getContent(), result.getTotalElements());
            redisTemplate.opsForHash().put(hashKey, field, objectMapper.writeValueAsString(pageData));
            redisTemplate.expire(hashKey, CACHE_TTL, TimeUnit.HOURS);
        } catch (Exception e) {
            System.err.println("Error caching product variants by store: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Page<ProductVariantResponse> searchByName(String name, int page, int size, String sortBy, String sortDir)
            throws Exception {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        // Sử dụng custom repository method để filter ở database level
        Page<ProductVariant> variantPage = productVariantRepository.searchByNameExcludingBannedStores(name,
                ProductVariant.VariantStatus.APPROVED.name(), pageable);

        return variantPage.map(ProductVariantResponse::fromProductVariant);
    }

    @Override
    @Transactional
    public void addProductVariantColors(String productVariantId, ColorOption colorOptionDTO, MultipartFile image)
            throws Exception {
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

        invalidateProductVariantCache(productVariant);
        productVariantRepository.save(productVariant);
    }

    @Override
    @Transactional
    public void updateProductVariantColors(String productVariantId, String colorId, ColorOption colorOptionDTO,
            MultipartFile imageFile)
            throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
        ProductVariant.ColorOption colorToUpdate = productVariant.getColors().stream()
                .filter(color -> color.getId().equals(colorId))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy màu sắc với id được cung cấp"));
        if (colorOptionDTO != null) {
            colorToUpdate.setColorName(colorOptionDTO.getColorName());
            colorToUpdate.setPrice(colorOptionDTO.getPrice());
            colorToUpdate.setStock(colorOptionDTO.getStock());
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (colorToUpdate.getImage() != null && !colorToUpdate.getImage().isEmpty()) {
                fileUploadService.deleteFile(colorToUpdate.getImage());
            }
            // Upload ảnh mới
            String newImageUrl = fileUploadService.uploadFile(imageFile, "product-variants");
            colorToUpdate.setImage(newImageUrl);
        }

        int totalStock = productVariant.getColors().stream().mapToInt(ProductVariant.ColorOption::getStock).sum();
        productVariant.setStock(totalStock);

        Long minPrice = productVariant.getColors().stream().mapToLong(ProductVariant.ColorOption::getPrice).min()
                .orElse(0L);
        productVariant.setPrice(minPrice);

        invalidateProductVariantCache(productVariant);

        productVariantRepository.save(productVariant);
    }

    @Override
    public void updateStock(String productVariantId, int newStock) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        // Chỉ cho phép cập nhật stock trực tiếp nếu không có màu sắc
        if (productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật stock trực tiếp cho sản phẩm có màu sắc. Hãy cập nhật stock của từng màu.");
        }

        if (newStock < 0) {
            throw new IllegalArgumentException("Stock không thể âm");
        }

        productVariant.setStock(newStock);
        invalidateProductVariantCache(productVariant);
        productVariantRepository.save(productVariant);
    }

    @Override
    public void updatePrice(String productVariantId, Long newPrice) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        // Chỉ cho phép cập nhật price trực tiếp nếu không có màu sắc
        if (productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật price trực tiếp cho sản phẩm có màu sắc. Hãy cập nhật của từng màu.");
        }

        if (newPrice != null && newPrice < 0) {
            throw new IllegalArgumentException("Price không thể âm");
        }

        productVariant.setPrice(newPrice);
        invalidateProductVariantCache(productVariant);
        productVariantRepository.save(productVariant);
    }

    public void updatePriceAndStock(String productVariantId, Long newPrice, int newStock) throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        // Chỉ cho phép cập nhật trực tiếp nếu không có màu sắc
        if (productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            throw new IllegalArgumentException(
                    "Không thể cập nhật price/stock trực tiếp cho sản phẩm có màu sắc. Hãy cập nhật của từng màu.");
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
        invalidateProductVariantCache(productVariant);
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

        invalidateProductVariantCache(productVariant);

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
            Long minPrice = productVariant.getColors().stream().mapToLong(ProductVariant.ColorOption::getPrice).min()
                    .orElse(0L);
            productVariant.setPrice(minPrice);
        } else {
            // Không có màu sắc -> stock và price hiện tại không thay đổi (do user thiết lập
            // thủ công)
            // Không cập nhật gì
        }

        productVariantRepository.save(productVariant);
    }

    @Override
    public Page<ProductVariantResponse> getVariantsByStatus(String status, Pageable pageable) {
        Page<ProductVariant> variantPage = productVariantRepository.findByStatus(status, pageable);
        return variantPage.map(ProductVariantResponse::fromProductVariant);
    }

    @Override
    public void updateVariantStatus(String variantId, String status) throws Exception {
        if (!ProductVariant.isValidStatus(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }
        ProductVariant productVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        productVariant.setStatus(status);
        invalidateProductVariantCache(productVariant);
        productVariantRepository.save(productVariant);
    }

    @Override
    public void rejectVariant(String variantId, String reason) throws Exception {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy biến thể"));

        variant.setStatus("REJECTED");
        variant.setRejectionReason(reason);
        invalidateProductVariantCache(variant);
        productVariantRepository.save(variant);
    }

    @Override
    public Page<ProductVariant> getAllProductVariantsB2C(String storeId, String status, Pageable pageable)
            throws Exception {
        Page<ProductVariant> productVariants;
        if (status != null && !status.isEmpty()) {
            if (!ProductVariant.isValidStatus(status)) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ");
            }
            productVariants = productVariantRepository.findByStoreIdAndStatus(storeId, status, pageable);
        } else {
            productVariants = productVariantRepository.findByStoreId(storeId, pageable);
        }
        return productVariants;
    }

    @Override
    public Page<ProductVariant> searchByStoreAndName(String storeId, String name, String status, Pageable pageable)
            throws Exception {
        Page<ProductVariant> productVariants;
        if (status != null && !status.isEmpty()) {
            if (!ProductVariant.isValidStatus(status)) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ");
            }
            productVariants = productVariantRepository.searchByStoreIdAndNameAndStatus(storeId, name, status, pageable);
        } else {
            productVariants = productVariantRepository.searchByStoreIdAndName(storeId, name, pageable);
        }
        return productVariants;
    }

    @Override
    @Transactional
    public void updateProductVariantImages(String productVariantId, List<MultipartFile> imageFiles, int indexPrimary)
            throws Exception {
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));

        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("Phải có ít nhất một ảnh");
        }

        if (indexPrimary < 0 || indexPrimary >= imageFiles.size()) {
            throw new IllegalArgumentException(
                    "Chỉ số ảnh chính không hợp lệ. Phải từ 0 đến " + (imageFiles.size() - 1));
        }

        // Xóa các ảnh cũ
        if (productVariant.getImageUrls() != null && !productVariant.getImageUrls().isEmpty()) {
            for (String imageUrl : productVariant.getImageUrls()) {
                fileUploadService.deleteFile(imageUrl);
            }
        }

        // Upload ảnh mới
        List<String> newImageUrls = fileUploadService.uploadFiles(imageFiles, "product-variants");
        productVariant.setImageUrls(newImageUrls);

        // Set ảnh chính theo chỉ số được cung cấp
        productVariant.setPrimaryImageUrl(newImageUrls.get(indexPrimary));
        invalidateProductVariantCache(productVariant);

        productVariantRepository.save(productVariant);
    }

    private String buildField(Pageable pageable) {
        return "page:" + pageable.getPageNumber() +
                ":size:" + pageable.getPageSize() +
                ":sort:" + pageable.getSort().toString();
    }

    private void invalidateProductVariantCache(ProductVariant variant) {
        redisTemplate.delete(CACHE_PREFIX + "category:" + variant.getCategoryName());

        redisTemplate.delete(CACHE_PREFIX + "category:" + variant.getCategoryName()
                + ":brand:" + variant.getBrandName());

        redisTemplate.delete(CACHE_PREFIX + "store:" + variant.getStoreId());

        redisTemplate.delete(CACHE_PREFIX + "latest");
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class PageData {
        private List<ProductVariantResponse> content;
        private long totalElements;
    }

    @Override
    public Map<String, Long> countProductVariantsByStatus(String storeId) throws Exception {
        Map<String, Long> statusCounts = new HashMap<>();

        long approved = productVariantRepository.countByStoreIdAndStatus(storeId,
                ProductVariant.VariantStatus.APPROVED.name());
        long pending = productVariantRepository.countByStoreIdAndStatus(storeId,
                ProductVariant.VariantStatus.PENDING.name());
        long rejected = productVariantRepository.countByStoreIdAndStatus(storeId,
                ProductVariant.VariantStatus.REJECTED.name());

        // Tổng tất cả biến thể (không kể DELETED)
        long total = approved + pending + rejected;

        long outOfStock = productVariantRepository.countByStoreIdAndStatusAndStockZero(storeId,
                ProductVariant.VariantStatus.APPROVED.name());

        statusCounts.put("total", total);
        statusCounts.put("approved", approved);
        statusCounts.put("pending", pending);
        statusCounts.put("outOfStock", outOfStock);

        return statusCounts;
    }

    @Override
    public ProductVariant getProductVariantById(String productVariantId) throws Exception {
        return productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy mẫu sản phẩm này"));
    }

    /**
     * Xóa toàn bộ cache product variants trong Redis
     * Sử dụng khi cần clear tất cả cache (ví dụ: khi ban/unban store)
     */
    public void clearAllProductVariantCache() {
        try {
            // Xóa tất cả keys bắt đầu bằng "product-variants:"
            var keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared all product variant cache. Total keys deleted: {}", keys.size());
            } else {
                log.info("No product variant cache keys found to delete");
            }
        } catch (Exception e) {
            log.error("Error clearing all product variant cache: {}", e.getMessage());
        }
    }

}