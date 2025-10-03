package com.example.e_commerce_techshop.responses.buyer;

public class CartItemCountResponse {
    private int totalItems;
    private boolean isEmpty;
        
    public CartItemCountResponse(int totalItems, boolean isEmpty) {
        this.totalItems = totalItems;
        this.isEmpty = isEmpty;
    }
        
    public int getTotalItems() { return totalItems; }
    public boolean isEmpty() { return isEmpty; }
}
