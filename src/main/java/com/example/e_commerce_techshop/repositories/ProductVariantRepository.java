package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProductVariantRepository extends MongoRepository<ProductVariant, String>, CustomProductVariantRepository {
    Page<ProductVariant> findByCategoryNameAndStatus(String categoryName, String status, Pageable pageable);

    Page<ProductVariant> findByCategoryNameAndBrandNameAndStatus(String category, String brand, String status,
            Pageable pageable);

    Page<ProductVariant> findByStoreIdAndStatus(String storeId, String status, Pageable pageable);

    Page<ProductVariant> findByStatus(String status, Pageable pageable);

    Page<ProductVariant> findByStoreId(String storeId, Pageable pageable);

    Page<ProductVariant> findByProductIdAndStatus(String productId, String status, Pageable pageable);

    @Query("{'name': {$regex: ?0, $options: 'i'}, 'status': ?1}")
    Page<ProductVariant> searchByNameAndStatus(String name, String status, Pageable pageable);

    @Query(value = "{ $and: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'status': ?1 } ] }", fields = "{ 'product': 1, 'name': 1, 'categoryName': 1, 'brandName': 1, 'storeId': 1, 'price': 1, 'description': 1, 'stock': 1, 'status': 1, 'rejectionReason': 1, 'attributes': 1, 'imageUrls': 1, 'primaryImageUrl': 1, 'colors': 1 }")
    Page<ProductVariant> searchByNameAndStatusWithProduct(String name, String status, Pageable pageable);

    @Query("{'storeId': ?0, 'name': {$regex: ?1, $options: 'i'}}")
    Page<ProductVariant> searchByStoreIdAndName(String storeId, String name, Pageable pageable);

    @Query("{'storeId': ?0, 'name': {$regex: ?1, $options: 'i'}, 'status': ?2}")
    Page<ProductVariant> searchByStoreIdAndNameAndStatus(String storeId, String name, String status, Pageable pageable);

    long countByStatus(String status);

    long countByStoreIdAndStatus(String storeId, String status);

    @Query(value = "{'storeId': ?0, 'status': ?1, 'stock': 0}", count = true)
    long countByStoreIdAndStatusAndStockZero(String storeId, String status);

    // Đếm sản phẩm sắp hết hàng (stock > 0 và <= 10)
    @Query(value = "{ 'storeId': ?0, 'status': ?1, 'stock': { '$gt': 0, '$lte': 10 } }", count = true)
    long countByStoreIdAndStatusAndLowStock(String storeId, String status);

    // Đếm sản phẩm hết hàng (stock = 0)
    @Query(value = "{ 'storeId': ?0, 'status': ?1, 'stock': 0 }", count = true)
    long countByStoreIdAndStatusAndOutOfStock(String storeId, String status);

    List<ProductVariant> findByStoreIdAndStatus(String storeId, String status);

    List<ProductVariant> findByStoreId(String storeId);
}
