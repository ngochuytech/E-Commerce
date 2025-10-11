package com.example.e_commerce_techshop.controllers.buyer.cart;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartDTO;
import com.example.e_commerce_techshop.dtos.buyer.cart.UpdateQuantityDTO;
import com.example.e_commerce_techshop.models.Cart;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.CartItemCountResponse;
import com.example.e_commerce_techshop.responses.buyer.CartResponse;
import com.example.e_commerce_techshop.services.cart.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/buyer/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final ICartService cartService;
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     * POST /api/v1/buyer/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestBody @Valid CartDTO cartDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Dữ liệu không hợp lệ: " + String.join(", ", errorMessages)));
            }
            
            String userEmail = getCurrentUserEmail();
            
            // Thêm vào giỏ hàng
            cartService.addToCart(userEmail, cartDTO);

            return ResponseEntity.ok(ApiResponse.ok("Thêm sản phẩm vào giỏ hàng thành công"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi thêm sản phẩm vào giỏ hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy tất cả sản phẩm trong giỏ hàng
     * GET /api/v1/buyer/cart
     */
    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            String userEmail = getCurrentUserEmail();
            Cart cart = cartService.getCart(userEmail);

            
            return ResponseEntity.ok(ApiResponse.ok(CartResponse.fromCart(cart)));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy giỏ hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * PUT /api/v1/buyer/cart/{cartItemId}
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable String cartItemId,
            @RequestBody @Valid UpdateQuantityDTO updateQuantityDTO,
            BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Dữ liệu không hợp lệ: " + String.join(", ", errorMessages)));
            }
            Integer quantity = updateQuantityDTO.getQuantity();
            System.out.println("DEBUG CartController: quantity = " + quantity); // Debug log
            if (quantity == null) {
                System.out.println("DEBUG CartController: quantity is null");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Số lượng không được để trống"));
            }
            
            String userEmail = getCurrentUserEmail();
            Cart cart = cartService.updateCartItem(userEmail, cartItemId, quantity);

            return ResponseEntity.ok(ApiResponse.ok(CartResponse.fromCart(cart)));

        } catch (Exception e) {
            e.printStackTrace(); // Debug log
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi cập nhật sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * DELETE /api/v1/buyer/cart/{cartItemId}
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable String cartItemId) {
        try {
            String userEmail = getCurrentUserEmail();
            cartService.removeCartItem(userEmail, cartItemId);

            return ResponseEntity.ok(ApiResponse.ok("Sản phẩm đã được xóa khỏi giỏ hàng"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xóa sản phẩm: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa toàn bộ giỏ hàng
     * DELETE /api/v1/buyer/cart/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart() {
        try {
            String userEmail = getCurrentUserEmail();
            cartService.clearCart(userEmail);

            return ResponseEntity.ok(ApiResponse.ok("Giỏ hàng đã được xóa"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xóa giỏ hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy số lượng sản phẩm trong giỏ hàng
     * GET /api/v1/buyer/cart/count
     */
    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount() {
        try {
            String userEmail = getCurrentUserEmail();
            int count = cartService.getCartItemCount(userEmail);
            boolean isEmpty = cartService.isCartEmpty(userEmail);
            
            return ResponseEntity.ok(ApiResponse.ok(
                new CartItemCountResponse(count, isEmpty)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy số lượng giỏ hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy user email từ JWT token
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Không tìm thấy thông tin người dùng");
        }
        return authentication.getName(); // Trong hệ thống hiện tại, getName() trả về email
    }
    
}
