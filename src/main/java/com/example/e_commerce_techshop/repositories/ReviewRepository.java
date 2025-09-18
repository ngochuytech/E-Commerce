package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    // Find reviews by store (through product variants)
    @Query("SELECT r FROM Review r JOIN r.productVariant pv JOIN pv.product p WHERE p.store.id = :storeId")
    List<Review> findByStoreId(@Param("storeId") String storeId);

    // Find reviews by product
    @Query("SELECT r FROM Review r WHERE r.productVariantId IN (SELECT pv.id FROM ProductVariant pv WHERE pv.product.id = :productId)")
    List<Review> findByProductId(@Param("productId") String productId);

    // Find reviews by product variant
    List<Review> findByProductVariantId(String productVariantId);

    // Find reviews by user
    List<Review> findByUserId(String userId);

    // Find reviews by rating
    List<Review> findByRating(Integer rating);

    // Count reviews by store
    @Query("SELECT COUNT(r) FROM Review r JOIN r.productVariant pv JOIN pv.product p WHERE p.store.id = :storeId")
    Long countByStoreId(@Param("storeId") String storeId);

    // Get average rating by store
    @Query("SELECT AVG(r.rating) FROM Review r JOIN r.productVariant pv JOIN pv.product p WHERE p.store.id = :storeId")
    Double getAverageRatingByStoreId(@Param("storeId") String storeId);

    // Get reviews without seller response
    @Query("SELECT r FROM Review r JOIN r.productVariant pv JOIN pv.product p WHERE p.store.id = :storeId AND r.sellerResponse IS NULL")
    List<Review> findPendingReviewsByStoreId(@Param("storeId") String storeId);
}