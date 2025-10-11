package com.example.e_commerce_techshop.services.cart;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartDTO;
import com.example.e_commerce_techshop.models.Cart;

public interface ICartService {
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    void addToCart(String userEmail, CartDTO cartDTO) throws Exception;
    
    /**
     * Lấy tất cả sản phẩm trong giỏ hàng
     */
    Cart getCart(String userEmail) throws Exception;
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    Cart updateCartItem(String userEmail, String cartItemId, Integer quantity) throws Exception;
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    void removeCartItem(String userEmail, String cartItemId) throws Exception;
    
    /**
     * Xóa toàn bộ giỏ hàng
     */
    void clearCart(String userEmail) throws Exception;
    
    /**
     * Kiểm tra giỏ hàng có trống không
     */
    boolean isCartEmpty(String userEmail);
    
    /**
     * Lấy số lượng sản phẩm trong giỏ hàng
     */
    int getCartItemCount(String userEmail);
}
