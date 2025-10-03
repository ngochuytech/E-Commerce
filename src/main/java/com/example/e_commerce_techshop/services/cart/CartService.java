package com.example.e_commerce_techshop.services.cart;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Cart;
import com.example.e_commerce_techshop.models.CartItem;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.repositories.CartItemRepository;
import com.example.e_commerce_techshop.repositories.CartRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.repositories.UserRepository;
import com.example.e_commerce_techshop.responses.buyer.CartResponse;
import com.example.e_commerce_techshop.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    
    // Giới hạn giỏ hàng
    private static final int MAX_CART_ITEMS = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 999;
    private static final double VAT_RATE = 0.1; // 10%
    
    @Override
    @Transactional
    public void addToCart(String userEmail, CartDTO cartDTO) throws Exception {
        // 1. Validate input
        validateCartInput(cartDTO);
        
        // 1.5. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với email: " + userEmail));

        // 2. Kiểm tra sản phẩm tồn tại
        for (CartDTO.CartItemDTO item : cartDTO.getItems()) {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm với ID: " + item.getProductVariantId()));
            // 2.1. Chỉ cho phép add-to-cart nếu store đã APPROVED
            if (productVariant.getProduct() == null || productVariant.getProduct().getStore() == null) {
                throw new IllegalArgumentException("Sản phẩm không gắn với cửa hàng hợp lệ");
            }
            String storeStatus = String.valueOf(productVariant.getProduct().getStore().getStatus());
            if (!"APPROVED".equalsIgnoreCase(storeStatus)) {
                throw new IllegalArgumentException("Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
            }
            // 2.2. Kiểm tra sản phẩm còn hàng
            if (productVariant.getStock() <= 0) {
                throw new DataNotFoundException("Sản phẩm đã hết hàng");
            }
        }
        
        // 3. Tìm hoặc tạo giỏ hàng cho user
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElse(Cart.builder()
                        .user(user)
                        .build());
        
        if (cart.getId() == null) {
            cart = cartRepository.save(cart);
        }
        
        // 4. Kiểm tra giới hạn giỏ hàng
        long currentCartItems = cartItemRepository.countByCartId(cart.getId());
        if (currentCartItems >= MAX_CART_ITEMS) {
            throw new IllegalArgumentException("Giỏ hàng đã đầy (tối đa " + MAX_CART_ITEMS + " sản phẩm)");
        }
        
        // 5. Batch processing
        
        // Lấy tất cả existing items trong một query
        List<CartItem> existingItems = cartItemRepository.findByCartId(cart.getId());
        Map<String, CartItem> existingItemMap = existingItems.stream()
                .collect(Collectors.toMap(
                    item -> item.getProductVariant().getId(),
                    item -> item
                ));
        
        // 6. Xử lý từng item
        List<CartItem> itemsToSave = new ArrayList<>();
        
        for (CartDTO.CartItemDTO itemDTO : cartDTO.getItems()) {
            ProductVariant productVariant = productVariantRepository.findById(itemDTO.getProductVariantId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm với ID: " + itemDTO.getProductVariantId()));
            
            CartItem cartItem = existingItemMap.get(itemDTO.getProductVariantId());
            
            if (cartItem != null) {
                // Update existing item
                int newQuantity = cartItem.getQuantity() + itemDTO.getQuantity();
                
                // Validation
                if (newQuantity > productVariant.getStock()) {
                    throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                            productVariant.getStock() + " sản phẩm)");
                }
                if (newQuantity > MAX_QUANTITY_PER_ITEM) {
                    throw new IllegalArgumentException("Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY_PER_ITEM);
                }
                
                cartItem.setQuantity(newQuantity);
                itemsToSave.add(cartItem);
            } else {
                // Create new item
                if (itemDTO.getQuantity() > productVariant.getStock()) {
                    throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                            productVariant.getStock() + " sản phẩm)");
                }
                
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .productVariant(productVariant)
                        .quantity(itemDTO.getQuantity())
                        .build();
                
                itemsToSave.add(newItem);
            }
        }
        
        // Batch save all items
        cartItemRepository.saveAll(itemsToSave);
    }
    
    @Override
    public CartResponse getCart(String userEmail) throws Exception {
        // Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        
        // Tìm giỏ hàng của user
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));
        
        return CartResponse.fromCart(cart);
    }
    
    @Override
    @Transactional
    public CartResponse updateCartItem(String userEmail, String cartItemId, Integer quantity) throws Exception {
        System.out.println("DEBUG CartService: userEmail = " + userEmail + ", cartItemId = " + cartItemId + ", quantity = " + quantity);
        // 1. Validate input
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
        
        // 1.5. Convert email to User object (y chang như B2C)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        
        // 2. Tìm giỏ hàng của user
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));
        
        // 3. Tìm card item
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại trong giỏ hàng"));
        
        // 4. Nếu số lượng = 0, xóa sản phẩm
        if (quantity == 0) {
            System.out.println("DEBUG updateCartItem: quantity = 0, removing item");
            
            // Xóa từ collection trước
            cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
            
            // Xóa từ database bằng custom query
            cartItemRepository.deleteByIdAndCartId(cartItemId, cart.getId());
            cartItemRepository.flush();
            
            // Cập nhật cart
            cartRepository.saveAndFlush(cart);
            
            return getCart(userEmail);
        }
        
        // 5. Kiểm tra sản phẩm còn hàng
        ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId())
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại"));

        // 5.1. Không cho thao tác với item thuộc store chưa APPROVED
        if (productVariant.getProduct() == null || productVariant.getProduct().getStore() == null) {
            throw new IllegalArgumentException("Sản phẩm không gắn với cửa hàng hợp lệ");
        }
        String storeStatus2 = String.valueOf(productVariant.getProduct().getStore().getStatus());
        if (!"APPROVED".equalsIgnoreCase(storeStatus2)) {
            throw new IllegalArgumentException("Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
        }
        
        if (productVariant.getStock() <= 0) {
            throw new DataNotFoundException("Sản phẩm đã hết hàng");
        }
        
        // 6. Kiểm tra số lượng không vượt quá tồn kho
        if (quantity > productVariant.getStock()) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                    productVariant.getStock() + " sản phẩm)");
        }
        
        // 7. Kiểm tra giới hạn số lượng
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY_PER_ITEM);
        }
        
        // 8. Cập nhật số lượng
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        
        return getCart(userEmail);
    }
    
    @Override
    @Transactional
    public void removeCartItem(String userEmail, String cartItemId) throws Exception {
        System.out.println("DEBUG removeCartItem: userEmail = " + userEmail + ", cartItemId = " + cartItemId);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));

        cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại trong giỏ hàng"));
        
        // Xóa cart item - sử dụng nhiều phương pháp để đảm bảo xóa thành công
        try {
            // Phương pháp 1: Xóa từ collection trong Cart entity (để tránh cascade conflict)
            cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
            
            // Phương pháp 2: Xóa bằng custom query (recommended cho entities có cascade)
            cartItemRepository.deleteByIdAndCartId(cartItemId, cart.getId());
            
            // Flush để đảm bảo thay đổi được commit ngay lập tức
            cartItemRepository.flush();
            
            // Cập nhật lại cart để đồng bộ
            cartRepository.saveAndFlush(cart);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa sản phẩm khỏi giỏ hàng: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void clearCart(String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        
        if (cart != null) {
            try {
                // Xóa từ collection trước
                cart.getCartItems().clear();
                
                // Xóa tất cả cart items từ database
                cartItemRepository.deleteByCartId(cart.getId());
                cartItemRepository.flush();
                
                // Cập nhật cart
                cartRepository.saveAndFlush(cart);
                
            } catch (Exception e) {
                System.err.println("ERROR clearing cart: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Lỗi khi xóa giỏ hàng: " + e.getMessage());
            }
        }
    }
    
    @Override
    public boolean isCartEmpty(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return true;
        
        return cartItemRepository.countByCartId(cart.getId()) == 0;
    }
    
    @Override
    public int getCartItemCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return 0;
        return cart.getCartItems().stream().mapToInt(CartItem::getQuantity).sum();
    }
    
    /**
     * Validate input cho cart
     */
    private void validateCartInput(CartDTO cartDTO) throws Exception {
        if (cartDTO == null) {
            throw new IllegalArgumentException("Dữ liệu giỏ hàng không được để trống");
        }

        for (CartDTO.CartItemDTO item : cartDTO.getItems()) {
            if (item.getProductVariantId() == null || item.getProductVariantId().trim().isEmpty()) {
                throw new IllegalArgumentException("ID sản phẩm không được để trống");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }

            if (item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                throw new IllegalArgumentException("Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY_PER_ITEM);
            }
        }
    }
}