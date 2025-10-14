package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);
    
    @Query("{'product.$id': ObjectId(?0)}")
    List<ProductVariant> findByProductIdWithObjectId(String productId);
    
    @Query("{'product.$id': ?0}")
    List<ProductVariant> findByProductIdWithQuery(String productId);

    List<ProductVariant> findByCategoryName(String categoryName);

    List<ProductVariant> findByCategoryNameAndBrandName(String category, String brand);

    List<ProductVariant> findByStockLessThan(Integer stock);

    List<ProductVariant> findByStockEquals(Integer stock);

    Page<ProductVariant> findByStoreId(String storeId, Pageable pageable);
}
