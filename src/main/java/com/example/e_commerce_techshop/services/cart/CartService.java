package com.example.e_commerce_techshop.services.cart;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartDTO;
import com.example.e_commerce_techshop.dtos.buyer.cart.CartItemDTO;
import com.example.e_commerce_techshop.dtos.buyer.cart.CartResponseDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.CardItem;
import com.example.e_commerce_techshop.models.Cart;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.repositories.CardItemRepository;
import com.example.e_commerce_techshop.repositories.CartRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.repositories.UserRepository;
import com.example.e_commerce_techshop.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    
    private final CartRepository cartRepository;
    private final CardItemRepository cardItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    
    // Giới hạn giỏ hàng
    private static final int MAX_CART_ITEMS = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 999;
    private static final double VAT_RATE = 0.1; // 10%
    
    @Override
    @Transactional
    public CartResponseDTO addToCart(String userEmail, CartDTO cartDTO) throws Exception {
        // 1. Validate input
        validateCartInput(cartDTO);
        
        // 1.5. Convert email to User object (y chang như B2C)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        
        // 2. Kiểm tra sản phẩm tồn tại
        ProductVariant productVariant = productVariantRepository.findById(cartDTO.getProductVariantId())
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại"));
        
        // 2.1. Chỉ cho phép add-to-cart nếu store đã APPROVED
        if (productVariant.getProduct() == null || productVariant.getProduct().getStore() == null) {
            throw new IllegalArgumentException("Sản phẩm không gắn với cửa hàng hợp lệ");
        }
        String storeStatus = String.valueOf(productVariant.getProduct().getStore().getStatus());
        if (!"APPROVED".equalsIgnoreCase(storeStatus)) {
            throw new IllegalArgumentException("Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
        }

        // 3. Kiểm tra sản phẩm còn hàng
        if (productVariant.getStock() <= 0) {
            throw new DataNotFoundException("Sản phẩm đã hết hàng");
        }
        
        // 4. Tìm hoặc tạo giỏ hàng cho user
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder()
                        .userId(userId)
                        .build());
        
        if (cart.getId() == null) {
            cart = cartRepository.save(cart);
        }
        
        // 5. Kiểm tra giới hạn giỏ hàng
        long currentCartItems = cardItemRepository.countByCartId(cart.getId());
        if (currentCartItems >= MAX_CART_ITEMS) {
            throw new IllegalArgumentException("Giỏ hàng đã đầy (tối đa " + MAX_CART_ITEMS + " sản phẩm)");
        }
        
        // 6. Tìm sản phẩm trong giỏ hàng hiện tại
        Optional<CardItem> existingCardItem = cardItemRepository.findByCartIdAndProductVariantId(
                cart.getId(), cartDTO.getProductVariantId());
        
        CardItem cardItem;
        if (existingCardItem.isPresent()) {
            // Sản phẩm đã có trong giỏ hàng - cập nhật số lượng
            cardItem = existingCardItem.get();
            int newQuantity = cardItem.getQuantity() + cartDTO.getQuantity();
            
            // Kiểm tra không vượt quá tồn kho
            if (newQuantity > productVariant.getStock()) {
                throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                        productVariant.getStock() + " sản phẩm)");
            }
            
            // Kiểm tra giới hạn số lượng
            if (newQuantity > MAX_QUANTITY_PER_ITEM) {
                throw new IllegalArgumentException("Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY_PER_ITEM);
            }
            
            cardItem.setQuantity(newQuantity);
            cardItemRepository.save(cardItem);
        } else {
            // Sản phẩm chưa có trong giỏ hàng - tạo mới
            // Kiểm tra số lượng không vượt quá tồn kho
            if (cartDTO.getQuantity() > productVariant.getStock()) {
                throw new IllegalArgumentException("Số lượng vượt quá tồn kho hiện có (" + 
                        productVariant.getStock() + " sản phẩm)");
            }
            
            cardItem = CardItem.builder()
                    .cartId(cart.getId())
                    .productVariantId(cartDTO.getProductVariantId())
                    .quantity(cartDTO.getQuantity())
                    .build();
            
            cardItemRepository.save(cardItem);
        }
        
        // 7. Trả về giỏ hàng đã cập nhật
        return getCart(userEmail);
    }
    
    @Override
    public CartResponseDTO getCart(String userEmail) throws Exception {
        // Convert email to User object (y chang như B2C)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        
        // Tìm giỏ hàng của user
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        
        if (cart == null) {
            return CartResponseDTO.builder()
                    .cartId("empty")
                    .userId(userId)
                    .items(new ArrayList<>())
                    .totalItems(0)
                    .subtotal(0L)
                    .tax(0L)
                    .total(0L)
                    .message("Giỏ hàng trống")
                    .build();
        }
        
        // Lấy tất cả items trong giỏ hàng
        List<CardItem> cardItems = cardItemRepository.findByCartId(cart.getId());
        
        if (cardItems.isEmpty()) {
            return CartResponseDTO.builder()
                    .cartId(cart.getId())
                    .userId(userId)
                    .items(new ArrayList<>())
                    .totalItems(0)
                    .subtotal(0L)
                    .tax(0L)
                    .total(0L)
                    .message("Giỏ hàng trống")
                    .build();
        }
        
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        long subtotal = 0;
        int totalItems = 0;
        
        for (CardItem cardItem : cardItems) {
            ProductVariant productVariant = productVariantRepository.findById(cardItem.getProductVariantId())
                    .orElse(null);
            
            if (productVariant != null) {
                CartItemDTO itemDTO = CartItemDTO.builder()
                        .cartItemId(cardItem.getId())
                        .productVariantId(cardItem.getProductVariantId())
                        .productName(productVariant.getProduct().getName())
                        .variantName(productVariant.getName())
                        .price(productVariant.getPrice())
                        .quantity(cardItem.getQuantity())
                        .subtotal(productVariant.getPrice() * cardItem.getQuantity())
                        .imageUrl(productVariant.getImageUrl())
                        .storeName(productVariant.getProduct().getStore().getName())
                        .storeId(productVariant.getProduct().getStore().getId())
                        .build();
                
                itemDTOs.add(itemDTO);
                subtotal += itemDTO.getSubtotal();
                totalItems += cardItem.getQuantity();
            }
        }
        
        long tax = Math.round(subtotal * VAT_RATE);
        long total = subtotal + tax;
        
        return CartResponseDTO.builder()
                .cartId(cart.getId())
                .userId(userId)
                .items(itemDTOs)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .tax(tax)
                .total(total)
                .message("Lấy giỏ hàng thành công")
                .build();
    }
    
    @Override
    @Transactional
    public CartResponseDTO updateCartItem(String userEmail, String cartItemId, Integer quantity) throws Exception {
        // 1. Validate input
        if (quantity == null || quantity < 0) {
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
        CardItem cardItem = cardItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại trong giỏ hàng"));
        
        // 4. Nếu số lượng = 0, xóa sản phẩm
        if (quantity == 0) {
            cardItemRepository.delete(cardItem);
            return getCart(userEmail);
        }
        
        // 5. Kiểm tra sản phẩm còn hàng
        ProductVariant productVariant = productVariantRepository.findById(cardItem.getProductVariantId())
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
        cardItem.setQuantity(quantity);
        cardItemRepository.save(cardItem);
        
        return getCart(userEmail);
    }
    
    @Override
    @Transactional
    public CartResponseDTO removeCartItem(String userEmail, String cartItemId) throws Exception {
        // Convert email to User object (y chang như B2C)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        
        // Tìm giỏ hàng của user
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));
        
        // Tìm và xóa card item
        CardItem cardItem = cardItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new DataNotFoundException("Sản phẩm không tồn tại trong giỏ hàng"));
        
        cardItemRepository.delete(cardItem);
        
        CartResponseDTO response = getCart(userEmail);
        response.setMessage("Đã xóa sản phẩm khỏi giỏ hàng");
        
        return response;
    }
    
    @Override
    @Transactional
    public CartResponseDTO clearCart(String userEmail) throws Exception {
        // Convert email to User object (y chang như B2C)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        
        // Tìm giỏ hàng của user
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        
        if (cart != null) {
            // Xóa tất cả card items
            cardItemRepository.deleteByCartId(cart.getId());
        }
        
        return CartResponseDTO.builder()
                .cartId("empty")
                .userId(userEmail)
                .items(new ArrayList<>())
                .totalItems(0)
                .subtotal(0L)
                .tax(0L)
                .total(0L)
                .message("Đã xóa toàn bộ giỏ hàng")
                .build();
    }
    
    @Override
    public boolean isCartEmpty(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return true;
        
        return cardItemRepository.countByCartId(cart.getId()) == 0;
    }
    
    @Override
    public int getCartItemCount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String userId = user.getId();
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return 0;
        
        return cardItemRepository.getTotalQuantityByCartId(cart.getId()).orElse(0);
    }
    
    /**
     * Validate input cho cart
     */
    private void validateCartInput(CartDTO cartDTO) throws Exception {
        if (cartDTO == null) {
            throw new IllegalArgumentException("Dữ liệu giỏ hàng không được để trống");
        }
        
        if (cartDTO.getProductVariantId() == null || cartDTO.getProductVariantId().trim().isEmpty()) {
            throw new IllegalArgumentException("ID sản phẩm không được để trống");
        }
        
        if (cartDTO.getQuantity() == null || cartDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        
        if (cartDTO.getQuantity() > MAX_QUANTITY_PER_ITEM) {
            throw new IllegalArgumentException("Số lượng tối đa cho mỗi sản phẩm là " + MAX_QUANTITY_PER_ITEM);
        }
    }
}