package com.example.e_commerce_techshop.repositories;

import com.example.e_commerce_techshop.dtos.ProductFilterDTO;
import com.example.e_commerce_techshop.models.Product;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ProductVariantAttribute;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    public List<ProductVariant> findProductsByFilters(ProductFilterDTO filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductVariant> query = cb.createQuery(ProductVariant.class);
        Root<ProductVariant> productVariants = query.from(ProductVariant.class);
        List<Predicate> predicates = new ArrayList<>();

        // Điều kiện category
        predicates.add(cb.equal(productVariants.get("product").get("category"), filter.getCategory()));

        // Điều kiện attributes (RAM, CPU, GPU hoặc Size_screen, Resolution)
        if (filter.getAttributes() != null && !filter.getAttributes().isEmpty()) {
            for (Map.Entry<String, String> entry : filter.getAttributes().entrySet()) {
                String attribute = entry.getKey();
                String value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    Join<ProductVariant, ProductVariantAttribute> pa = productVariants.join("attributes");
                    predicates.add(cb.equal(pa.get("attribute").get("name"), attribute));
                    // Tạo điều kiện OR cho từng giá trị với LIKE
                    List<Predicate> valuePredicates = new ArrayList<>();
                    valuePredicates.add(cb.like(pa.get("value"), "%" + value + "%"));

                    predicates.add(cb.or(valuePredicates.toArray(new Predicate[0])));
                }
            }
        }

        query.select(productVariants).distinct(true).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).getResultList();
    }
}