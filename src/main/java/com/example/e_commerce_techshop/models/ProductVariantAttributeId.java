package com.example.e_commerce_techshop.models;

import java.io.Serializable;
import java.util.Objects;

public class ProductVariantAttributeId implements Serializable {
    private String productVariant;

    private Long attribute;

    public ProductVariantAttributeId() {
    }

    public ProductVariantAttributeId(String product, Long attribute) {
        this.productVariant = product;
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariantAttributeId that = (ProductVariantAttributeId) o;
        return Objects.equals(productVariant, that.productVariant) &&
                Objects.equals(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productVariant, attribute);
    }
}
