package com.example.e_commerce_techshop.services.cart;

import java.util.List;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartDTO;
import com.example.e_commerce_techshop.models.Cart;
import com.example.e_commerce_techshop.models.User;

public interface ICartService {
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    void addToCart(User user, List<CartDTO> cartDTO) throws Exception;
    
    /**
     * Lấy tất cả sản phẩm trong giỏ hàng
     */
    Cart getCart(User user) throws Exception;
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    Cart updateCartItem(String userEmail, String productVariantId, String colorId, Integer quantity) throws Exception;
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    void removeCartItem(User user, String cartItemId) throws Exception;
    
    /**
     * Xóa toàn bộ giỏ hàng
     */
    void clearCart(User user) throws Exception;
    
    /**
     * Xóa các sản phẩm đã chọn khỏi giỏ hàng
     */
    void removeSelectedItemsByIds(User user, List<String> cartItemIds) throws Exception;
    
    /**
     * Kiểm tra giỏ hàng có trống không
     */
    boolean isCartEmpty(String userEmail);
    
    /**
     * Lấy số lượng sản phẩm trong giỏ hàng
     */
    int getCartItemCount(String userEmail);
}
