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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/buyer/cart")
@RequiredArgsConstructor
@Tag(name = "Buyer Cart Management", description = "Shopping cart management APIs for buyers - Handle adding, updating, removing items, and cart operations")
@SecurityRequirement(name = "bearerAuth")
public class BuyerCartController {

    private final ICartService cartService;

    /**
     * Thêm sản phẩm vào giỏ hàng
     * POST /api/v1/buyer/cart/add
     */
    @PostMapping("/add")
    @Operation(summary = "Add products to cart", description = "Add one or multiple products to the shopping cart. If product already exists, quantity will be updated")
    public ResponseEntity<?> addToCart(
            @Parameter(description = "List of cart items to add including product variant ID and quantity", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = CartDTO.class)))) @RequestBody @Valid List<CartDTO> cartDTO,
            @Parameter(hidden = true) BindingResult result,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
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
    }

    /**
     * Lấy tất cả sản phẩm trong giỏ hàng
     * GET /api/v1/buyer/cart
     */
    @GetMapping
    @Operation(summary = "Get shopping cart", description = "Retrieve all items in the current user's shopping cart with detailed product information and totals")
    public ResponseEntity<?> getCart(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        Cart cart = cartService.getCart(currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.ok(CartResponse.fromCart(cart)));
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * PUT /api/v1/buyer/cart/{productVariantId}
     */
    @PutMapping("/{productVariantId}")
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of a specific product variant in the shopping cart")
    public ResponseEntity<?> updateCartItem(
            @Parameter(description = "ID of the product variant to update", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String productVariantId,
            @Parameter(description = "Color ID of the product variant (optional)", required = false, example = "red") @RequestParam(required = false) String colorId,
            @Parameter(description = "New quantity for the cart item", required = true, content = @Content(schema = @Schema(implementation = UpdateQuantityDTO.class))) @RequestBody @Valid UpdateQuantityDTO updateQuantityDTO,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        Integer quantity = updateQuantityDTO.getQuantity();
        if (quantity == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Số lượng không được để trống"));
        }

        Cart cart = cartService.updateCartItem(currentUser.getEmail(), productVariantId, colorId, quantity);

        return ResponseEntity.ok(ApiResponse.ok(CartResponse.fromCart(cart)));

    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * DELETE /api/v1/buyer/cart/{cartItemId}
     */
    @DeleteMapping("/{productVariantId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific product variant completely from the shopping cart")
    public ResponseEntity<?> removeCartItem(
            @Parameter(description = "ID of the product variant to remove", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String productVariantId,
            @Parameter(description = "Color ID of the product variant (optional)", required = false, example = "red") @RequestParam(required = false) String colorId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        cartService.removeCartItem(currentUser.getEmail(), productVariantId, colorId);

        return ResponseEntity.ok(ApiResponse.ok("Sản phẩm đã được xóa khỏi giỏ hàng"));

    }

    /**
     * Xóa toàn bộ giỏ hàng
     * DELETE /api/v1/buyer/cart/clear
     */
    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire cart", description = "Remove all items from the shopping cart. This action cannot be undone")
    public ResponseEntity<?> clearCart(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        cartService.clearCart(currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.ok("Giỏ hàng đã được xóa"));

    }

    /**
     * Lấy số lượng sản phẩm trong giỏ hàng
     * GET /api/v1/buyer/cart/count
     */
    @GetMapping("/count")
    @Operation(summary = "Get cart item count", description = "Retrieve the total number of items in the shopping cart and whether the cart is empty. Useful for cart badge display")
    public ResponseEntity<?> getCartItemCount(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        int count = cartService.getCartItemCount(currentUser.getEmail());
        boolean isEmpty = cartService.isCartEmpty(currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.ok(
                new CartItemCountResponse(count, isEmpty)));
    }

}
