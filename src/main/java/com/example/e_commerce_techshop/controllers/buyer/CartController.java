package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartDTO;
import com.example.e_commerce_techshop.dtos.buyer.cart.UpdateQuantityDTO;
import com.example.e_commerce_techshop.models.Cart;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.buyer.CartItemCountResponse;
import com.example.e_commerce_techshop.responses.buyer.CartResponse;
import com.example.e_commerce_techshop.services.cart.ICartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @RequestBody @Valid List<CartDTO> cartDTO,
            BindingResult result,
            @AuthenticationPrincipal User currentUser) {
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Dữ liệu không hợp lệ: " + String.join(", ", errorMessages)));
            }
            
            // Thêm vào giỏ hàng
            cartService.addToCart(currentUser.getEmail(), cartDTO);

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
    public ResponseEntity<?> getCart(@AuthenticationPrincipal User currentUser) {
        try {
            Cart cart = cartService.getCart(currentUser.getEmail());

            return ResponseEntity.ok(ApiResponse.ok(CartResponse.fromCart(cart)));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy giỏ hàng: " + e.getMessage()));
        }
    }
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * PUT /api/v1/buyer/cart/{productVariantId}
     */
    @PutMapping("/{productVariantId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable String productVariantId,
            @RequestBody @Valid UpdateQuantityDTO updateQuantityDTO,
            @AuthenticationPrincipal User currentUser) {
        try {
            Integer quantity = updateQuantityDTO.getQuantity();
            if (quantity == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Số lượng không được để trống"));
            }
            
            Cart cart = cartService.updateCartItem(currentUser.getEmail(), productVariantId, quantity);

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
    @DeleteMapping("/{productVariantId}")
    public ResponseEntity<?> removeCartItem(
            @PathVariable String productVariantId,
            @AuthenticationPrincipal User currentUser) {
        try {
            cartService.removeCartItem(currentUser.getEmail(), productVariantId);

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
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal User currentUser) {
        try {
            cartService.clearCart(currentUser.getEmail());

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
    public ResponseEntity<?> getCartItemCount(@AuthenticationPrincipal User currentUser) {
        try {
            int count = cartService.getCartItemCount(currentUser.getEmail());
            boolean isEmpty = cartService.isCartEmpty(currentUser.getEmail());
            
            return ResponseEntity.ok(ApiResponse.ok(
                new CartItemCountResponse(count, isEmpty)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy số lượng giỏ hàng: " + e.getMessage()));
        }
    }
    
}
