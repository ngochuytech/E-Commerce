package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomProductVariantRepositoryImpl implements CustomProductVariantRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<ProductVariant> searchByNameExcludingBannedStores(String name, String status, Pageable pageable) {
        // Lookup Product
        LookupOperation lookupProduct = LookupOperation.newLookup()
                .from("products")
                .localField("product.$id")
                .foreignField("_id")
                .as("productData");

        // Unwind productData
        UnwindOperation unwindProduct = Aggregation.unwind("productData");

        // Lookup Store from Product
        LookupOperation lookupStore = LookupOperation.newLookup()
                .from("stores")
                .localField("productData.store.$id")
                .foreignField("_id")
                .as("storeData");

        // Unwind storeData
        UnwindOperation unwindStore = Aggregation.unwind("storeData");

        // Match conditions: name regex, status, and store not banned
        MatchOperation matchOperation = Aggregation.match(
                new Criteria().andOperator(
                        Criteria.where("name").regex(name, "i"),
                        Criteria.where("status").is(status),
                        Criteria.where("storeData.status").ne(Store.StoreStatus.BANNED.name())
                )
        );

        // Count total elements
        Aggregation countAggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.count().as("total")
        );

        AggregationResults<org.bson.Document> countResults = mongoTemplate.aggregate(
                countAggregation, "product_variants", org.bson.Document.class
        );

        long total = 0;
        if (countResults.getMappedResults().size() > 0) {
            total = countResults.getMappedResults().get(0).getInteger("total", 0);
        }

        // Get paginated results
        Aggregation aggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.sort(pageable.getSort()),
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<ProductVariant> results = mongoTemplate.aggregate(
                aggregation, "product_variants", ProductVariant.class
        );

        List<ProductVariant> variants = results.getMappedResults();

        return new PageImpl<>(variants, pageable, total);
    }
    @Override
    public Page<ProductVariant> findByStatusExcludingBannedStores(String status, Pageable pageable) {
        // Lookup Product
        LookupOperation lookupProduct = LookupOperation.newLookup()
                .from("products")
                .localField("product.$id")
                .foreignField("_id")
                .as("productData");

        // Unwind productData
        UnwindOperation unwindProduct = Aggregation.unwind("productData");

        // Lookup Store from Product
        LookupOperation lookupStore = LookupOperation.newLookup()
                .from("stores")
                .localField("productData.store.$id")
                .foreignField("_id")
                .as("storeData");

        // Unwind storeData
        UnwindOperation unwindStore = Aggregation.unwind("storeData");

        // Match conditions: status and store not banned
        MatchOperation matchOperation = Aggregation.match(
                new Criteria().andOperator(
                        Criteria.where("status").is(status),
                        Criteria.where("storeData.status").ne(Store.StoreStatus.BANNED.name())
                )
        );

        // Count total elements
        Aggregation countAggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.count().as("total")
        );

        AggregationResults<org.bson.Document> countResults = mongoTemplate.aggregate(
                countAggregation, "product_variants", org.bson.Document.class
        );

        long total = 0;
        if (countResults.getMappedResults().size() > 0) {
            total = countResults.getMappedResults().get(0).getInteger("total", 0);
        }

        // Get paginated results
        Aggregation aggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<ProductVariant> results = mongoTemplate.aggregate(
                aggregation, "product_variants", ProductVariant.class
        );

        return new PageImpl<>(results.getMappedResults(), pageable, total);
    }

    @Override
    public Page<ProductVariant> findByCategoryNameAndStatusExcludingBannedStores(String categoryName, String status, Pageable pageable) {
        // Lookup Product
        LookupOperation lookupProduct = LookupOperation.newLookup()
                .from("products")
                .localField("product.$id")
                .foreignField("_id")
                .as("productData");

        // Unwind productData
        UnwindOperation unwindProduct = Aggregation.unwind("productData");

        // Lookup Store from Product
        LookupOperation lookupStore = LookupOperation.newLookup()
                .from("stores")
                .localField("productData.store.$id")
                .foreignField("_id")
                .as("storeData");

        // Unwind storeData
        UnwindOperation unwindStore = Aggregation.unwind("storeData");

        // Match conditions: categoryName, status and store not banned
        MatchOperation matchOperation = Aggregation.match(
                new Criteria().andOperator(
                        Criteria.where("categoryName").is(categoryName),
                        Criteria.where("status").is(status),
                        Criteria.where("storeData.status").ne(Store.StoreStatus.BANNED.name())
                )
        );

        // Count total elements
        Aggregation countAggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.count().as("total")
        );

        AggregationResults<org.bson.Document> countResults = mongoTemplate.aggregate(
                countAggregation, "product_variants", org.bson.Document.class
        );

        long total = 0;
        if (countResults.getMappedResults().size() > 0) {
            total = countResults.getMappedResults().get(0).getInteger("total", 0);
        }

        // Get paginated results
        Aggregation aggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<ProductVariant> results = mongoTemplate.aggregate(
                aggregation, "product_variants", ProductVariant.class
        );

        return new PageImpl<>(results.getMappedResults(), pageable, total);
    }

    @Override
    public Page<ProductVariant> findByCategoryNameAndBrandNameAndStatusExcludingBannedStores(String categoryName, String brandName, String status, Pageable pageable) {
        // Lookup Product
        LookupOperation lookupProduct = LookupOperation.newLookup()
                .from("products")
                .localField("product.$id")
                .foreignField("_id")
                .as("productData");

        // Unwind productData
        UnwindOperation unwindProduct = Aggregation.unwind("productData");

        // Lookup Store from Product
        LookupOperation lookupStore = LookupOperation.newLookup()
                .from("stores")
                .localField("productData.store.$id")
                .foreignField("_id")
                .as("storeData");

        // Unwind storeData
        UnwindOperation unwindStore = Aggregation.unwind("storeData");

        // Match conditions: categoryName, brandName, status and store not banned
        MatchOperation matchOperation = Aggregation.match(
                new Criteria().andOperator(
                        Criteria.where("categoryName").is(categoryName),
                        Criteria.where("brandName").is(brandName),
                        Criteria.where("status").is(status),
                        Criteria.where("storeData.status").ne(Store.StoreStatus.BANNED.name())
                )
        );

        // Count total elements
        Aggregation countAggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.count().as("total")
        );

        AggregationResults<org.bson.Document> countResults = mongoTemplate.aggregate(
                countAggregation, "product_variants", org.bson.Document.class
        );

        long total = 0;
        if (countResults.getMappedResults().size() > 0) {
            total = countResults.getMappedResults().get(0).getInteger("total", 0);
        }

        // Get paginated results
        Aggregation aggregation = Aggregation.newAggregation(
                lookupProduct,
                unwindProduct,
                lookupStore,
                unwindStore,
                matchOperation,
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<ProductVariant> results = mongoTemplate.aggregate(
                aggregation, "product_variants", ProductVariant.class
        );

        return new PageImpl<>(results.getMappedResults(), pageable, total);
    }
}
