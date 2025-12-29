package com.example.e_commerce_techshop.controllers.buyer;

import com.example.e_commerce_techshop.annotations.RequireActiveAccount;
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
@RequireActiveAccount
@Tag(name = "Buyer Cart Management", description = "API quan lý giỏ hàng cho người mua - Xử lý thêm, cập nhật, xóa và lấy thông tin giỏ hàng")
@SecurityRequirement(name = "bearerAuth")
public class BuyerCartController {

    private final ICartService cartService;

    @GetMapping
    @Operation(summary = "Lấy giỏ hàng", description = "Lấy tất cả các mục trong giỏ hàng của người dùng hiện tại với thông tin chi tiết về sản phẩm và tổng cộng")
    public ResponseEntity<?> getCart(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        Cart cart = cartService.getCart(currentUser);

        return ResponseEntity.ok(ApiResponse.ok(CartResponse.fromCart(cart)));
    }

    @GetMapping("/count")
    @Operation(summary = "Lấy số lượng sản phẩm trong giỏ hàng", description = "Lấy tổng số lượng các mục trong giỏ hàng và kiểm tra xem giỏ hàng có trống hay không. Hữu ích cho việc hiển thị biểu tượng giỏ hàng")
    public ResponseEntity<?> getCartItemCount(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        int count = cartService.getCartItemCount(currentUser.getEmail());
        boolean isEmpty = cartService.isCartEmpty(currentUser.getEmail());

        return ResponseEntity.ok(ApiResponse.ok(
                new CartItemCountResponse(count, isEmpty)));
    }

    @PostMapping("/add")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng", description = "Thêm một hoặc nhiều sản phẩm vào giỏ hàng. Nếu sản phẩm đã tồn tại, số lượng sẽ được cập nhật")
    public ResponseEntity<?> addToCart(
            @Parameter(description = "Danh sách các mục giỏ hàng cần thêm bao gồm ID biến thể sản phẩm và số lượng", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = CartDTO.class)))) @RequestBody @Valid List<CartDTO> cartDTO,
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
        cartService.addToCart(currentUser, cartDTO);

        return ResponseEntity.ok(ApiResponse.ok("Thêm sản phẩm vào giỏ hàng thành công"));
    }

    @PutMapping("/{productVariantId}")
    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng", description = "Cập nhật số lượng của một biến thể sản phẩm cụ thể trong giỏ hàng")
    public ResponseEntity<?> updateCartItem(
            @Parameter(description = "ID của biến thể sản phẩm", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String productVariantId,
            @Parameter(description = "ID màu của biến thể sản phẩm (tùy chọn)", required = false, example = "red") @RequestParam(required = false) String colorId,
            @Parameter(description = "Số lượng mới cho mục giỏ hàng", required = true, content = @Content(schema = @Schema(implementation = UpdateQuantityDTO.class))) @RequestBody @Valid UpdateQuantityDTO updateQuantityDTO,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        Integer quantity = updateQuantityDTO.getQuantity();
        if (quantity == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Số lượng không được để trống"));
        }

        Cart cart = cartService.updateCartItem(currentUser.getEmail(), productVariantId, colorId, quantity);

        return ResponseEntity.ok(ApiResponse.ok(CartResponse.fromCart(cart)));

    }

    @DeleteMapping("/{cartItemId}")
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng", description = "Xóa một biến thể sản phẩm cụ thể hoàn toàn khỏi giỏ hàng")
    public ResponseEntity<?> removeCartItem(
            @Parameter(description = "ID của mục giỏ hàng cần xóa", required = true) @PathVariable String cartItemId,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        cartService.removeCartItem(currentUser, cartItemId);

        return ResponseEntity.ok(ApiResponse.ok("Sản phẩm đã được xóa khỏi giỏ hàng"));

    }

    @DeleteMapping("")
    @Operation(summary = "Xóa nhiều sản phẩm khỏi giỏ hàng", description = "Xóa nhiều biến thể sản phẩm cụ thể hoàn toàn khỏi giỏ hàng")
    public ResponseEntity<?> removeManyCartItems(
            @Parameter(description = "Danh sách ID các mục giỏ hàng cần xóa", required = true, content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))) @RequestBody List<String> cartItemIds,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        cartService.removeSelectedItemsByIds(currentUser, cartItemIds);
        return ResponseEntity.ok(ApiResponse.ok("Các sản phẩm đã được xóa khỏi giỏ hàng"));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Xóa toàn bộ giỏ hàng", description = "Xóa tất cả các mục trong giỏ hàng. Hành động này không thể hoàn tác")
    public ResponseEntity<?> clearCart(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) throws Exception {
        cartService.clearCart(currentUser);

        return ResponseEntity.ok(ApiResponse.ok("Giỏ hàng đã được xóa"));

    }

}
