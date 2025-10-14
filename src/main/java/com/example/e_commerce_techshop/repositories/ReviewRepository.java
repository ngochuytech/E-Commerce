package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    @Query("{ 'productVariant.product.store.$id': ?0 }")
    List<Review> findByStoreId(String storeId);
    
    @Query("{ 'order.store.$id': ?0 }")
    List<Review> findByOrder_StoreId(String storeId);

    @Query("{ 'productVariant.product.$id': ?0 }")
    List<Review> findByProductId(String productId);

    List<Review> findByProductVariantId(String productVariantId);

    List<Review> findByUserId(String userId);

    List<Review> findByRating(Integer rating);

    @Query(value = "{ 'productVariant.product.store.$id': ?0 }", count = true)
    Long countByStoreId(String storeId);

    @Query(value = "{ 'order.store.$id': ?0 }", fields = "{ 'rating': 1 }")
    List<Review> findReviewsForAverageByStoreId(String storeId);
    
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
}