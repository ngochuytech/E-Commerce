package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    // Find reviews by store (through product variants)
    @Query("{ 'productVariant.product.store.$id': ?0 }")
    List<Review> findByStoreId(String storeId);
    
    @Query("{ 'order.store.$id': ?0 }")
    List<Review> findByOrder_StoreId(String storeId);

    // Find reviews by product
    @Query("{ 'productVariant.product.$id': ?0 }")
    List<Review> findByProductId(String productId);

    // Find reviews by product variant
    List<Review> findByProductVariantId(String productVariantId);

    // Find reviews by user
    List<Review> findByUserId(String userId);

    // Find reviews by rating
    List<Review> findByRating(Integer rating);

    // Count reviews by store
    @Query(value = "{ 'productVariant.product.store.$id': ?0 }", count = true)
    Long countByStoreId(String storeId);

    // Get average rating by store - MongoDB aggregation
    @Query(value = "{ 'order.store.$id': ?0 }", fields = "{ 'rating': 1 }")
    List<Review> findReviewsForAverageByStoreId(String storeId);
    
    // Calculate average rating by store ID
    default Double getAverageRatingByStoreId(String storeId) {
        List<Review> reviews = findReviewsForAverageByStoreId(storeId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
    
    // Get reviews without seller response
//    @Query("{ 'productVariant.product.store.$id': ?0, 'sellerResponse': null }")
//    List<Review> findPendingReviewsByStoreId(String storeId);
}