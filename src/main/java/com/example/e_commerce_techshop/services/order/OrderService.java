package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.buyer.cart.CartResponseDTO;
import com.example.e_commerce_techshop.dtos.buyer.order.CheckoutDTO;
import com.example.e_commerce_techshop.dtos.buyer.order.OrderItemDTO;
import com.example.e_commerce_techshop.dtos.buyer.order.OrderResponseDTO;
import com.example.e_commerce_techshop.dtos.buyer.order.OrderSummaryDTO;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final ICartService cartService;
    
    @Override
    @Transactional
    public OrderResponseDTO checkout(String userEmail, CheckoutDTO checkoutDTO) throws Exception {
        // 1. Convert email to User object (y chang như B2C)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String buyerId = user.getId();
        
        // 2. Validate address exists
        addressRepository.findById(checkoutDTO.getAddressId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy địa chỉ với ID: " + checkoutDTO.getAddressId()));
        
        // 3. Validate payment method
        if (!isValidPaymentMethod(checkoutDTO.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + checkoutDTO.getPaymentMethod());
        }
        
        // 4. Lấy cart hiện tại
        CartResponseDTO cart = cartService.getCart(userEmail);
        
        // 5. Validate cart không rỗng
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống, không thể đặt hàng");
        }
        
        // 6. Validate từng sản phẩm trong cart
        for (var cartItem : cart.getItems()) {
            ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariantId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm: " + cartItem.getProductVariantId()));
            
            // Kiểm tra stock
            if (productVariant.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() + 
                        ", Số lượng còn lại: " + productVariant.getStock());
            }
            
            // Kiểm tra trạng thái sản phẩm
            if (!"ACTIVE".equals(productVariant.getProduct().getStatus().toString())) {
                throw new IllegalArgumentException("Sản phẩm không khả dụng: " + productVariant.getName());
            }
            
            // Kiểm tra trạng thái store
            if (!"APPROVED".equals(productVariant.getProduct().getStore().getStatus().toString())) {
                throw new IllegalArgumentException("Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
            }
        }
        
        // 7. Tạo Order - Lấy store_id từ sản phẩm đầu tiên (vì hiện tại DB chỉ hỗ trợ 1 store per order)
        ProductVariant firstProduct = productVariantRepository.findById(cart.getItems().get(0).getProductVariantId()).get();
        String storeId = firstProduct.getProduct().getStore().getId();
        
        // 7.1. Tính tổng tiền trước giảm
        BigDecimal cartTotal = BigDecimal.valueOf(cart.getTotal());

        // 7.2. Áp mã khuyến mãi cấp độ đơn (nếu có và hợp lệ)
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (checkoutDTO.getPromotionId() != null && !checkoutDTO.getPromotionId().isBlank()) {
            // Validate promotion thuộc store này và còn hiệu lực
            Promotion promo = promotionRepository.findById(checkoutDTO.getPromotionId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy khuyến mãi"));
            if (!storeId.equals(promo.getStore().getId())) {
                throw new IllegalArgumentException("Khuyến mãi không thuộc cửa hàng của đơn");
            }
            if (!"ACTIVE".equalsIgnoreCase(promo.getStatus())) {
                throw new IllegalArgumentException("Khuyến mãi không còn hiệu lực");
            }
            LocalDateTime now = LocalDateTime.now();
            if (promo.getStartDate().isAfter(now) || promo.getEndDate().isBefore(now)) {
                throw new IllegalArgumentException("Khuyến mãi nằm ngoài thời gian áp dụng");
            }
            long minOrder = promo.getMinOrderValue() != null ? promo.getMinOrderValue() : 0L;
            if (cartTotal.longValue() < minOrder) {
                throw new IllegalArgumentException("Giá trị đơn chưa đạt tối thiểu cho khuyến mãi");
            }

            if ("PERCENTAGE".equalsIgnoreCase(promo.getType())) {
                discountAmount = cartTotal.multiply(BigDecimal.valueOf(promo.getDiscountValue() == null ? 0 : promo.getDiscountValue())
                        .divide(BigDecimal.valueOf(100)));
            } else if ("FIXED_AMOUNT".equalsIgnoreCase(promo.getType())) {
                discountAmount = BigDecimal.valueOf(promo.getDiscountValue() == null ? 0 : promo.getDiscountValue());
            }

            // Áp trần giảm tối đa nếu có
            if (promo.getMaxDiscountValue() != null && promo.getMaxDiscountValue() > 0) {
                discountAmount = discountAmount.min(BigDecimal.valueOf(promo.getMaxDiscountValue()));
            }
        }

        BigDecimal totalAfterDiscount = cartTotal.subtract(discountAmount).max(BigDecimal.ZERO);

        Order order = Order.builder()
                .buyerId(buyerId)
                .storeId(storeId)
                .promotionId(checkoutDTO.getPromotionId())
                .totalPrice(totalAfterDiscount)
                .addressId(checkoutDTO.getAddressId())
                .paymentMethod(checkoutDTO.getPaymentMethod())
                .status("PENDING")
                .build();
        
        order = orderRepository.save(order);
        
        // 8. Tạo OrderItems từ CartItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (var cartItem : cart.getItems()) {
            ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariantId()).get();
            
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productVariantId(cartItem.getProductVariantId())
                    .quantity(cartItem.getQuantity())
                    .price(BigDecimal.valueOf(productVariant.getPrice().longValue())) // Price locking
                    .build();
            
            orderItems.add(orderItem);
        }
        
        orderItemRepository.saveAll(orderItems);
        
        // 9. Clear cart
        cartService.clearCart(userEmail);
        
        // 10. Return order response
        return getOrderDetail(userEmail, order.getId());
    }
    
    @Override
    public Page<OrderSummaryDTO> getOrderHistory(String userEmail, int page, int size, String status) throws Exception {
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
        
        // 4. Convert to DTO
        return orders.map(this::convertToOrderSummaryDTO);
    }
    
    @Override
    public OrderResponseDTO getOrderDetail(String userEmail, String orderId) throws Exception {
        // 1. Convert email to User object
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng với Email: " + userEmail));
        String buyerId = user.getId();
        
        // 2. Tìm order
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));
        
        // 3. Lấy order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        
        // 4. Convert to DTO
        return convertToOrderResponseDTO(order, orderItems);
    }
    
    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(String userEmail, String orderId) throws Exception {
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
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        // 5. Hoàn trả stock
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId()).get();
            productVariant.setStock(productVariant.getStock() + item.getQuantity());
            productVariantRepository.save(productVariant);
        }
        
        // 6. Return updated order
        return convertToOrderResponseDTO(order, order.getOrderItems());
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
    
    private OrderSummaryDTO convertToOrderSummaryDTO(Order order) {
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .itemCount((int) orderItemRepository.countByOrderId(order.getId()))
                .createdAt(order.getCreatedAt())
                .build();
    }
    
    private OrderResponseDTO convertToOrderResponseDTO(Order order, List<OrderItem> orderItems) {
        // Optimize: Get all product variants at once to avoid N+1 queries
        List<String> productVariantIds = orderItems.stream()
                .map(OrderItem::getProductVariantId)
                .collect(Collectors.toList());
        
        List<ProductVariant> productVariants = productVariantRepository.findAllById(productVariantIds);
        Map<String, ProductVariant> productVariantMap = productVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, pv -> pv));
        
        List<OrderItemDTO> orderItemDTOs = orderItems.stream()
                .map(item -> convertToOrderItemDTO(item, productVariantMap.get(item.getProductVariantId())))
                .collect(Collectors.toList());
        
        return OrderResponseDTO.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .storeId(order.getStoreId())
                .promotionId(order.getPromotionId())
                .totalPrice(order.getTotalPrice())
                .addressId(order.getAddressId())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItemDTOs)
                .build();
    }
    
    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem, ProductVariant productVariant) {
        String productName = productVariant != null ? productVariant.getName() : "Unknown Product";
        String productImage = productVariant != null ? productVariant.getImageUrl() : null;
        String storeName = "Unknown Store";
        String storeLogo = null;
        
        // Lấy thông tin store từ Product -> Store
        if (productVariant != null && productVariant.getProduct() != null && productVariant.getProduct().getStore() != null) {
            Store store = productVariant.getProduct().getStore();
            storeName = store.getName();
            storeLogo = store.getLogo_url();
        }
        
        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .productVariantId(orderItem.getProductVariantId())
                .productName(productName)
                .productImage(productImage)
                .storeName(storeName)
                .storeLogo(storeLogo)
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .subtotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .build();
    }
    
    /**
     * Validate payment method
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        List<String> validMethods = List.of("COD", "BANK_TRANSFER", "CREDIT_CARD", "E_WALLET");
        return validMethods.contains(paymentMethod.toUpperCase());
    }
}