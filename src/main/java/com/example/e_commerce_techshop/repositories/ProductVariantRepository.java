package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    // Sử dụng query method naming convention thay vì @Query annotation
    List<ProductVariant> findByProductId(String productId);
    
    // Alternative method sử dụng @Query với ObjectId
    @Query("{'product.$id': ObjectId(?0)}")
    List<ProductVariant> findByProductIdWithObjectId(String productId);
    
    // Alternative method sử dụng @Query với string
    @Query("{'product.$id': ?0}")
    List<ProductVariant> findByProductIdWithQuery(String productId);

    // Sửa lại query cho category - sử dụng @Query vì @DBRef
    @Query("{'product.category.$id': ?0}")
    List<ProductVariant> findByProductCategoryId(String categoryId);

    @Query("{'product.category.id': ?0, 'product.brand.id': ?1}")
    List<ProductVariant> findByProductCategoryAndProductBrandName(String category, String brand);

    List<ProductVariant> findByStockLessThan(Integer stock);

    List<ProductVariant> findByStockEquals(Integer stock);

    @Query("{'product.$id': ?0}")
    Page<ProductVariant> findByProductStoreId(String storeId, Pageable pageable);
}
