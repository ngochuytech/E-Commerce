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

    Page<ProductVariant> findByCategoryNameAndStatus(String categoryName, String status, Pageable pageable);

    Page<ProductVariant> findByCategoryNameAndBrandNameAndStatus(String category, String brand, String status, Pageable pageable);

    Page<ProductVariant> findByStockLessThan(Integer stock, Pageable pageable);

    List<ProductVariant> findByStockEquals(Integer stock);

    Page<ProductVariant> findByStoreIdAndStatus(String storeId, String status, Pageable pageable);

    Page<ProductVariant> findByStatus(String status, Pageable pageable);

    Page<ProductVariant> findByProductIdAndStatus(String productId, String status, Pageable pageable);

    @Query("{'name': {$regex: ?0, $options: 'i'}, 'status': ?1}")
    Page<ProductVariant> searchByNameAndStatus(String name, String status, Pageable pageable);
}
