package com.example.e_commerce_techshop.services.cart;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.Cart;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.repositories.CartRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.repositories.user.UserRepository;
import com.example.e_commerce_techshop.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    
    // Giới hạn giỏ hàng
    private static final int MAX_CART_ITEMS = 20;
    private static final int MAX_QUANTITY_PER_ITEM = 999;
    
    @Override
    @Transactional
    public void addToCart(User user, List<CartDTO> cartDTO) throws Exception {
        // 1. Validate input
        validateCartInput(cartDTO);
        
        // 2. Kiểm tra sản phẩm tồn tại
        for (CartDTO item : cartDTO) {
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
            int availableStock = getAvailableStock(productVariant, item.getColorId());
            if (availableStock <= 0) {
                throw new DataNotFoundException("Sản phẩm đã hết hàng");
            }
        }
        
        // 3. Tìm hoặc tạo giỏ hàng cho user
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElse(Cart.builder()
                        .user(user)
                        .cartItems(new ArrayList<>())
                        .build());
        
        if (cart.getId() == null) {
            cart = cartRepository.save(cart);
        }
        
        // 4. Kiểm tra giới hạn giỏ hàng
        if (cart.getCartItems().size() >= MAX_CART_ITEMS) {
            throw new IllegalArgumentException("Giỏ hàng đã đầy (tối đa " + MAX_CART_ITEMS + " sản phẩm)");
        }
        
        // 5. Xử lý từng item với embedded approach
        for (CartDTO itemDTO : cartDTO) {
            ProductVariant productVariant = productVariantRepository.findById(itemDTO.getProductVariantId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm với ID: " + itemDTO.getProductVariantId()));
            
            // Tìm item đã tồn tại trong cart (so sánh cả productVariantId và colorId)
            Cart.CartItemEmbedded existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getProductVariant().getId().equals(itemDTO.getProductVariantId()) &&
                            ((item.getColorId() == null && itemDTO.getColorId() == null) ||
                             (item.getColorId() != null && item.getColorId().equals(itemDTO.getColorId()))))
                    .findFirst()
                    .orElse(null);
            
            if (existingItem != null) {
                // Update existing item
                int newQuantity = existingItem.getQuantity() + itemDTO.getQuantity();
                
                // Validation với stock theo màu sắc
                int availableStock = getAvailableStock(productVariant, itemDTO.getColorId());
                if (newQuantity > availableStock) {
                    throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                            availableStock + " sản phẩm)");
                }
                if (newQuantity > MAX_QUANTITY_PER_ITEM) {
                    throw new IllegalArgumentException("Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY_PER_ITEM);
                }
                
                existingItem.setQuantity(newQuantity);
                existingItem.setUnitPrice(getProductPrice(productVariant, itemDTO.getColorId()));
                // Cập nhật colorId nếu có thay đổi
                existingItem.setColorId(itemDTO.getColorId());
            } else {
                // Create new embedded item
                int availableStock = getAvailableStock(productVariant, itemDTO.getColorId());
                if (itemDTO.getQuantity() > availableStock) {
                    throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                            availableStock + " sản phẩm)");
                }
                
                Cart.CartItemEmbedded newItem = Cart.CartItemEmbedded.builder()
                        .id(UUID.randomUUID().toString())
                        .productVariant(productVariant)
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(getProductPrice(productVariant, itemDTO.getColorId()))
                        .colorId(itemDTO.getColorId())
                        .build();
                
                cart.getCartItems().add(newItem);
            }
        }
        
        // Save cart với embedded items
        cartRepository.save(cart);
    }
    
    @Override
    public Cart getCart(User user) throws Exception {
        // Tìm giỏ hàng của user
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));
        
        return cart;
    }
    
    @Override
    @Transactional
    public Cart updateCartItem(String userEmail, String productVariantId, String colorId, Integer quantity) throws Exception {
        // 1. Validate input
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
        
        // 1.5. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        
        // 2. Tìm giỏ hàng của user
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));
        
        // 3. Tìm cart item trong embedded list (so sánh cả productVariantId và colorId)
        Cart.CartItemEmbedded cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(productVariantId) &&
                        ((item.getColorId() == null && colorId == null) ||
                         (item.getColorId() != null && item.getColorId().equals(colorId))))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại trong giỏ hàng"));
        
        // 4. Nếu số lượng = 0, xóa sản phẩm
        if (quantity == 0) {
            cart.getCartItems().removeIf(item -> item.getProductVariant().getId().equals(productVariantId) &&
                    ((item.getColorId() == null && colorId == null) ||
                     (item.getColorId() != null && item.getColorId().equals(colorId))));
            cartRepository.save(cart);
            return getCart(user);
        }
        
        // 5. Kiểm tra sản phẩm còn hàng
        ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId())
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại"));

        // 5.1. Không cho thao tác với item thuộc store chưa APPROVED
        if (productVariant.getProduct() == null || productVariant.getProduct().getStore() == null) {
            throw new IllegalArgumentException("Sản phẩm không gắn với cửa hàng hợp lệ");
        }
        String storeStatus = productVariant.getProduct().getStore().getStatus();
        if (!"APPROVED".equalsIgnoreCase(storeStatus)) {
            throw new IllegalArgumentException("Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
        }
        
        int availableStock = getAvailableStock(productVariant, colorId);
        if (availableStock <= 0) {
            throw new DataNotFoundException("Sản phẩm đã hết hàng");
        }
        
        // 6. Kiểm tra số lượng không vượt quá tồn kho
        if (quantity > availableStock) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                    availableStock + " sản phẩm)");
        }
        
        // 7. Kiểm tra giới hạn số lượng
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY_PER_ITEM);
        }
        
        // 8. Cập nhật số lượng trong embedded item
        cartItem.setQuantity(quantity);
        cartItem.setUnitPrice(getProductPrice(productVariant, colorId)); // Update price theo màu sắc
        cartItem.setColorId(colorId); // Update colorId
        
        cartRepository.save(cart);
        
        return getCart(user);
    }
    
    @Override
    @Transactional
    public void removeCartItem(User user, String cartItemId) throws Exception {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));

        if(!cart.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Không thể xóa sản phẩm khỏi giỏ hàng của người dùng khác");
        }

        cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));

        // Save cart
        cartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public void clearCart(User user) throws Exception {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElse(null);
        
        if (cart != null) {
            cart.getCartItems().clear();
            cartRepository.save(cart);
        }
    }
    
    @Override
    @Transactional
    public void removeSelectedItemsByIds(User user, List<String> cartItemsIds) throws Exception {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));

        if(!cart.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Không thể xóa sản phẩm khỏi giỏ hàng của người dùng khác");
        }
        
        // Xóa các items được chọn
        for (String cartItemId : cartItemsIds) {
            cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
        }

        cartRepository.save(cart);
    }

    @Override
    public boolean isCartEmpty(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return true;
        
        return cart.getCartItems().isEmpty();
    }
    
    @Override
    public int getCartItemCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return 0;
        return cart.getCartItems().stream().mapToInt(Cart.CartItemEmbedded::getQuantity).sum();
    }
    
    /**
     * Validate input cho cart
     */
    private void validateCartInput(List<CartDTO> cartDTO) throws Exception {
        if (cartDTO == null || cartDTO.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu giỏ hàng không được để trống");
        }

        for (CartDTO item : cartDTO) {
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
    
    /**
     * Lấy số lượng stock có sẵn cho sản phẩm (có thể theo màu)
     */
    private int getAvailableStock(ProductVariant productVariant, String colorId) {
        if (colorId != null && productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            // Có chọn màu sắc cụ thể
            return productVariant.getColors().stream()
                    .filter(color -> color.getId().equals(colorId))
                    .findFirst()
                    .map(ProductVariant.ColorOption::getStock)
                    .orElse(0);
        } else {
            // Không chọn màu sắc -> dùng stock tổng
            return productVariant.getStock();
        }
    }

    /**
     * Lấy giá của sản phẩm (có thể theo màu)
     */
    private Long getProductPrice(ProductVariant productVariant, String colorId) {
        if (colorId != null && productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            // Có chọn màu sắc cụ thể
            return productVariant.getColors().stream()
                    .filter(color -> color.getId().equals(colorId))
                    .findFirst()
                    .map(ProductVariant.ColorOption::getPrice)
                    .orElse(productVariant.getPrice()); // Fallback về giá chung nếu không tìm thấy màu
        } else {
            // Không chọn màu sắc -> dùng giá chung
            return productVariant.getPrice();
        }
    }
}