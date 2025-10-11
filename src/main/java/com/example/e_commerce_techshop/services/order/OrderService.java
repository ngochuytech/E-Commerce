package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.OrderDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.*;
import com.example.e_commerce_techshop.repositories.*;
import com.example.e_commerce_techshop.services.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PromotionRepository promotionRepository;
    private final StoreRepository storeRepository;
    private final CartRepository cartRepository;
    private final ICartService cartService;
    
    @Override
    @Transactional
    public List<Order> checkout(String userEmail, OrderDTO orderDTO) throws Exception {
        // 1. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        
        // 2. Validate address exists

        // 3. Validate payment method
        if (!isValidPaymentMethod(orderDTO.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + orderDTO.getPaymentMethod());
        }
        
        // 4. Lấy cart hiện tại
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));

        // 5. Validate cart không rỗng
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống, không thể đặt hàng");
        }
        
        // 6. Validate từng sản phẩm trong cart
        for (var cartItem : cart.getCartItems()) {
            ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm: " + cartItem.getProductVariant().getId()));

            // Kiểm tra stock
            if (productVariant.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() + 
                        ", Số lượng còn lại: " + productVariant.getStock());
            }
            
            // Kiểm tra trạng thái sản phẩm
            if (!"ACTIVE".equals(productVariant.getProduct().getStatus())) {
                throw new IllegalArgumentException("Sản phẩm không khả dụng: " + productVariant.getName());
            }
            
            // Kiểm tra trạng thái store
            if (!"APPROVED".equals(productVariant.getProduct().getStore().getStatus())) {
                throw new IllegalArgumentException("Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
            }
        }
        
        // 7. Group cart items theo store
        Map<String, List<Cart.CartItemEmbedded>> itemsByStore = new HashMap<>();
        
        for (var cartItem : cart.getCartItems()) {
            ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm: " + cartItem.getProductVariant().getId()));

            String storeId = productVariant.getProduct().getStore().getId();
            itemsByStore.computeIfAbsent(storeId, k -> new ArrayList<>()).add(cartItem);
        }
        
        List<Order> orders = new ArrayList<>();
        
        // 8. Tạo 1 đơn hàng cho mỗi store
        for (Map.Entry<String, List<Cart.CartItemEmbedded>> storeEntry : itemsByStore.entrySet()) {
            String storeId = storeEntry.getKey();
            List<Cart.CartItemEmbedded> storeItems = storeEntry.getValue();
            
            // 8.1. Lấy thông tin store
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng với ID: " + storeId));
            
            // 8.2. Tính tổng tiền cho store này
            BigDecimal storeTotal = BigDecimal.ZERO;
            for (Cart.CartItemEmbedded item : storeItems) {
                ProductVariant productVariant = productVariantRepository.findById(item.getProductVariant().getId()).get();
                BigDecimal itemTotal = BigDecimal.valueOf(productVariant.getPrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                storeTotal = storeTotal.add(itemTotal);
            }
            
            // 8.3. Xử lý promotion (nếu có)
            Promotion promotion = null;
            BigDecimal finalTotal = storeTotal;
            
            if (orderDTO.getPromotionCode() != null && !orderDTO.getPromotionCode().trim().isEmpty()) {
                Optional<Promotion> promotionOpt = promotionRepository.findByCode(orderDTO.getPromotionCode());
                if (promotionOpt.isPresent()) {
                    promotion = promotionOpt.get();
                    
                    // Validate promotion conditions
                    if (isPromotionValid(promotion, storeTotal, store)) {
                        finalTotal = applyPromotion(storeTotal, promotion);
                    } else {
                        throw new IllegalArgumentException("Mã giảm giá không hợp lệ hoặc không đủ điều kiện áp dụng");
                    }
                } else {
                    throw new IllegalArgumentException("Mã giảm giá không tồn tại: " + orderDTO.getPromotionCode());
                }
            }
    
            // 8.4. Tạo Order cho store này
            Order order = Order.builder()
                    .buyer(user)
                    .store(store)
                    .promotion(promotion)
                    .totalPrice(finalTotal)
                    .address(Address.builder()
                            .province(orderDTO.getAddress().getProvince())
                            .district(orderDTO.getAddress().getDistrict())
                            .ward(orderDTO.getAddress().getWard())
                            .homeAddress(orderDTO.getAddress().getHomeAddress())
                            .build()
                    )
                    .paymentMethod(orderDTO.getPaymentMethod())
                    .status("PENDING")
                    .note(orderDTO.getNote())
                    .build();
            
            order = orderRepository.save(order);
            
            // 8.5. Tạo OrderItems cho store này và trừ stock
            List<OrderItem> orderItems = new ArrayList<>();
            for (Cart.CartItemEmbedded cartItem : storeItems) {
                ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId()).get();
                
                int newStock = productVariant.getStock() - cartItem.getQuantity();
                if (newStock < 0) {
                    throw new IllegalArgumentException("Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() + 
                            ", Số lượng còn lại: " + productVariant.getStock() + ", Số lượng yêu cầu: " + cartItem.getQuantity());
                }
                productVariant.setStock(newStock);
                productVariantRepository.save(productVariant);
                
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productVariant(productVariant)
                        .quantity(cartItem.getQuantity())
                        .price(productVariant.getPrice())
                        .build();
                
                orderItems.add(orderItem);
            }
            
            orderItemRepository.saveAll(orderItems);
            order.setOrderItems(orderItems);
            // 8.6.  Add order into order list
            orders.add(order);
        }
        
        // 9. Clear cart
        cartService.clearCart(userEmail);
        
        // 10. Return danh sách orders
        return orders;
    }
    
    @Override
    public Page<Order> getOrderHistory(String userEmail, int page, int size, String status) throws Exception {
        // 1. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String buyerId = user.getId();
        
        // 2. Tạo Pageable
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 3. Query orders
        Page<Order> orders;
        if (status == null || "ALL".equals(status)) {
            orders = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable);
        } else {
            orders = orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(buyerId, status, pageable);
        }

        // 4. Convert to Response
        return orders;
    }
    
    @Override
    public Order getOrderDetail(String userEmail, String orderId) throws Exception {
        // 1. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String buyerId = user.getId();
        
        // 2. Tìm order
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));
        
        // 3. Convert to OrderResponse của Buyer
        return order;
    }
    
    @Override
    @Transactional
    public Order cancelOrder(String userEmail, String orderId) throws Exception {
        // 1. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String buyerId = user.getId();
        
        // 2. Tìm order
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));
        
        // 3. Kiểm tra có thể hủy không
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING");
        }
        
        // 4. Cập nhật status
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        
        // 5. Hoàn trả stock
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariant().getId()).get();
            productVariant.setStock(productVariant.getStock() + item.getQuantity());
            productVariantRepository.save(productVariant);
        }
        
        // 6. Return updated order
        return order;
    }
    
    
    @Override
    public Map<String, Long> getOrderCount(String userEmail) throws Exception {
        // 1. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String buyerId = user.getId();
        
        // 2. Đếm orders theo status
        Map<String, Long> counts = new HashMap<>();
        counts.put("total", orderRepository.countByBuyerId(buyerId));
        counts.put("pending", orderRepository.countByBuyerIdAndStatus(buyerId, "PENDING"));
        counts.put("confirmed", orderRepository.countByBuyerIdAndStatus(buyerId, "CONFIRMED"));
        counts.put("shipping", orderRepository.countByBuyerIdAndStatus(buyerId, "SHIPPING"));
        counts.put("delivered", orderRepository.countByBuyerIdAndStatus(buyerId, "DELIVERED"));
        counts.put("cancelled", orderRepository.countByBuyerIdAndStatus(buyerId, "CANCELLED"));
        
        return counts;
    }
    
    /**
     * Validate payment method
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        List<String> validMethods = List.of("COD", "BANK_TRANSFER", "CREDIT_CARD", "E_WALLET", "VNPAY", "MOMO");
        return validMethods.contains(paymentMethod.toUpperCase());
    }
    
    /**
     * Validate promotion conditions
     */
    private boolean isPromotionValid(Promotion promotion, BigDecimal orderTotal, Store store) {
        LocalDateTime now = LocalDateTime.now();
        
        // Kiểm tra thời gian hiệu lực
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            return false;
        }
        
        // Kiểm tra trạng thái
        if (!"ACTIVE".equals(promotion.getStatus())) {
            return false;
        }
        
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (promotion.getMinOrderValue() != null) {
            BigDecimal minOrderValue = BigDecimal.valueOf(promotion.getMinOrderValue());
            if (orderTotal.compareTo(minOrderValue) < 0) {
                return false;
            }
        }
        
        // Kiểm tra phạm vi áp dụng (store-specific hoặc platform-wide)
        if (promotion.getStore() != null && !promotion.getStore().getId().equals(store.getId())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Apply promotion discount to order total
     */
    private BigDecimal applyPromotion(BigDecimal orderTotal, Promotion promotion) {
        BigDecimal discount = BigDecimal.ZERO;
        
        if ("PERCENTAGE".equals(promotion.getType())) {
            // Giảm giá theo phần trăm
            BigDecimal discountValue = BigDecimal.valueOf(promotion.getDiscountValue());
            discount = orderTotal.multiply(discountValue.divide(BigDecimal.valueOf(100)));
            
            // Áp dụng giới hạn giảm giá tối đa (nếu có)
            if (promotion.getMaxDiscountValue() != null) {
                BigDecimal maxDiscount = BigDecimal.valueOf(promotion.getMaxDiscountValue());
                if (discount.compareTo(maxDiscount) > 0) {
                    discount = maxDiscount;
                }
            }
        } else if ("FIXED_AMOUNT".equals(promotion.getType())) {
            // Giảm giá cố định
            discount = BigDecimal.valueOf(promotion.getDiscountValue());
            
            // Không được giảm nhiều hơn giá trị đơn hàng
            if (discount.compareTo(orderTotal) > 0) {
                discount = orderTotal;
            }
        }
        
        return orderTotal.subtract(discount);
    }
}