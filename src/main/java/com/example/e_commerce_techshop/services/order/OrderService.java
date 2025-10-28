package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
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
    public List<Order> checkout(User user, OrderDTO orderDTO) throws Exception {
        // 2. Validate payment method
        if (!isValidPaymentMethod(orderDTO.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + orderDTO.getPaymentMethod());
        }
        
        // 3. Validate danh sách sản phẩm được chọn
        if (orderDTO.getSelectedItems() == null || orderDTO.getSelectedItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm để thanh toán");
        }
        
        // 4. Lấy cart hiện tại
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));

        // 5. Validate cart không rỗng
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống, không thể đặt hàng");
        }
        
        // 6. Lọc ra các cart items được chọn
        List<Cart.CartItemEmbedded> selectedCartItems = new ArrayList<>();
        for (OrderDTO.SelectedCartItem selectedItem : orderDTO.getSelectedItems()) {
            Cart.CartItemEmbedded cartItem = cart.getCartItems().stream()
                    .filter(item -> item.getProductVariant().getId().equals(selectedItem.getProductVariantId()) &&
                            ((selectedItem.getColorId() == null && item.getColorId() == null) ||
                             (selectedItem.getColorId() != null && selectedItem.getColorId().equals(item.getColorId()))))
                    .findFirst()
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng: " + 
                            selectedItem.getProductVariantId() + 
                            (selectedItem.getColorId() != null ? " (màu: " + selectedItem.getColorId() + ")" : "")));
            
            selectedCartItems.add(cartItem);
        }
        
        // 7. Validate từng sản phẩm được chọn
        for (var cartItem : selectedCartItems) {
            ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm: " + cartItem.getProductVariant().getId()));

            // Kiểm tra stock theo màu sắc nếu có
            int availableStock = getAvailableStock(productVariant, cartItem.getColorId());
            if (availableStock < cartItem.getQuantity()) {
                String colorInfo = cartItem.getColorId() != null ? " (màu: " + cartItem.getColorId() + ")" : "";
                throw new IllegalArgumentException("Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() + 
                        colorInfo + ", Số lượng còn lại: " + availableStock);
            }
            
            // Kiểm tra trạng thái store
            if (!Store.StoreStatus.APPROVED.name().equals(productVariant.getProduct().getStore().getStatus())) {
                throw new IllegalArgumentException("Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
            }
        }
        
        // 8. Group selected cart items theo store
        Map<String, List<Cart.CartItemEmbedded>> itemsByStore = new HashMap<>();
        
        for (var cartItem : selectedCartItems) {
            ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm: " + cartItem.getProductVariant().getId()));

            String storeId = productVariant.getProduct().getStore().getId();
            itemsByStore.computeIfAbsent(storeId, k -> new ArrayList<>()).add(cartItem);
        }
        
        // 9. Validate và xử lý Platform Promotions (áp dụng cho TẤT CẢ orders)
        Promotion platformOrderPromotion = null;
        Promotion platformShippingPromotion = null;
        
        // 9.0. Tính số orders sẽ được tạo ra
        int numberOfOrders = itemsByStore.size();
        
        if (orderDTO.getPlatformPromotions() != null) {
            // 9.1. Platform ORDER Promotion
            if (orderDTO.getPlatformPromotions().getOrderPromotionCode() != null && 
                !orderDTO.getPlatformPromotions().getOrderPromotionCode().trim().isEmpty()) {
                
                String code = orderDTO.getPlatformPromotions().getOrderPromotionCode();
                platformOrderPromotion = promotionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá sàn không tồn tại: " + code));
                
                // Validate: Phải là PLATFORM và ORDER
                if (!Promotion.Issuer.PLATFORM.name().equals(platformOrderPromotion.getIssuer())) {
                    throw new IllegalArgumentException("Mã " + code + " không phải là mã của sàn");
                }
                if (!Promotion.ApplicableFor.ORDER.name().equals(platformOrderPromotion.getApplicableFor())) {
                    throw new IllegalArgumentException("Mã " + code + " không phải là mã giảm giá đơn hàng");
                }
                
                // Validate status, time
                if (!Promotion.PromotionStatus.ACTIVE.name().equals(platformOrderPromotion.getStatus())) {
                    throw new IllegalArgumentException("Mã giảm giá sàn không còn hiệu lực");
                }
                
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(platformOrderPromotion.getStartDate()) || now.isAfter(platformOrderPromotion.getEndDate())) {
                    throw new IllegalArgumentException("Mã giảm giá sàn đã hết hạn hoặc chưa bắt đầu");
                }
                
                // Validate usage limit với số orders sẽ được tạo
                if (platformOrderPromotion.getUsageLimit() != null) {
                    int remainingUsage = platformOrderPromotion.getUsageLimit() - platformOrderPromotion.getUsedCount();
                    if (remainingUsage < numberOfOrders) {
                        throw new IllegalArgumentException(
                            String.format("Mã giảm giá sàn chỉ còn %d lượt sử dụng nhưng bạn đang đặt hàng từ %d cửa hàng. " +
                                         "Vui lòng giảm số lượng cửa hàng hoặc bỏ mã giảm giá này.", 
                                         remainingUsage, numberOfOrders)
                        );
                    }
                }
            }
            
            // 9.2. Platform SHIPPING Promotion
            if (orderDTO.getPlatformPromotions().getShippingPromotionCode() != null && 
                !orderDTO.getPlatformPromotions().getShippingPromotionCode().trim().isEmpty()) {
                
                String code = orderDTO.getPlatformPromotions().getShippingPromotionCode();
                platformShippingPromotion = promotionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Mã giảm phí ship sàn không tồn tại: " + code));
                
                // Validate: Phải là PLATFORM và SHIPPING
                if (!Promotion.Issuer.PLATFORM.name().equals(platformShippingPromotion.getIssuer())) {
                    throw new IllegalArgumentException("Mã " + code + " không phải là mã của sàn");
                }
                if (!Promotion.ApplicableFor.SHIPPING.name().equals(platformShippingPromotion.getApplicableFor())) {
                    throw new IllegalArgumentException("Mã " + code + " không phải là mã giảm phí vận chuyển");
                }
                
                // Validate status, time
                if (!Promotion.PromotionStatus.ACTIVE.name().equals(platformShippingPromotion.getStatus())) {
                    throw new IllegalArgumentException("Mã giảm phí ship sàn không còn hiệu lực");
                }
                
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(platformShippingPromotion.getStartDate()) || now.isAfter(platformShippingPromotion.getEndDate())) {
                    throw new IllegalArgumentException("Mã giảm phí ship sàn đã hết hạn hoặc chưa bắt đầu");
                }
                
                // Validate usage limit với số orders sẽ được tạo
                if (platformShippingPromotion.getUsageLimit() != null) {
                    int remainingUsage = platformShippingPromotion.getUsageLimit() - platformShippingPromotion.getUsedCount();
                    if (remainingUsage < numberOfOrders) {
                        throw new IllegalArgumentException(
                            String.format("Mã giảm phí ship sàn chỉ còn %d lượt sử dụng nhưng bạn đang đặt hàng từ %d cửa hàng. " +
                                         "Vui lòng giảm số lượng cửa hàng hoặc bỏ mã giảm phí ship này.", 
                                         remainingUsage, numberOfOrders)
                        );
                    }
                }
            }
        }
        
        List<Order> orders = new ArrayList<>();
        
        // 10. Tạo 1 đơn hàng cho mỗi store
        for (Map.Entry<String, List<Cart.CartItemEmbedded>> storeEntry : itemsByStore.entrySet()) {
            String storeId = storeEntry.getKey();
            List<Cart.CartItemEmbedded> storeItems = storeEntry.getValue();
            
            // 10.1. Lấy thông tin store
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng với ID: " + storeId));
            
            // 10.2. Tính tổng tiền cho store này
            BigDecimal storeTotal = BigDecimal.ZERO;
            for (Cart.CartItemEmbedded item : storeItems) {
                ProductVariant productVariant = productVariantRepository.findById(item.getProductVariant().getId()).get();
                Long itemPrice = getProductPrice(productVariant, item.getColorId());
                BigDecimal itemTotal = BigDecimal.valueOf(itemPrice)
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                storeTotal = storeTotal.add(itemTotal);
            }
            
            // 10.3. Xử lý promotion - Thứ tự: Platform ORDER → Store ORDER → Platform SHIPPING → Store SHIPPING
            List<Promotion> appliedPromotions = new ArrayList<>();
            BigDecimal orderDiscount = BigDecimal.ZERO;
            BigDecimal shippingDiscount = BigDecimal.ZERO;
            
            BigDecimal currentTotal = storeTotal; // Track giá trị sau mỗi lần discount
            
            // 10.3.1. Platform ORDER Promotion (áp dụng trước)
            if (platformOrderPromotion != null) {
                // Check minOrderValue
                if (platformOrderPromotion.getMinOrderValue() == null || 
                    currentTotal.compareTo(BigDecimal.valueOf(platformOrderPromotion.getMinOrderValue())) >= 0) {
                    
                    BigDecimal platformDiscount = calculateDiscount(currentTotal, platformOrderPromotion);
                    orderDiscount = orderDiscount.add(platformDiscount);
                    currentTotal = currentTotal.subtract(platformDiscount);
                    appliedPromotions.add(platformOrderPromotion);
                }
            }
            
            // 10.3.2. Store ORDER Promotion (áp dụng sau platform)
            if (orderDTO.getStorePromotions() != null && orderDTO.getStorePromotions().containsKey(storeId)) {
                OrderDTO.StorePromotions storePromo = orderDTO.getStorePromotions().get(storeId);
                
                if (storePromo.getOrderPromotionCode() != null && !storePromo.getOrderPromotionCode().trim().isEmpty()) {
                    Promotion storeOrderPromotion = promotionRepository.findByCode(storePromo.getOrderPromotionCode())
                            .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá đơn hàng không tồn tại: " + storePromo.getOrderPromotionCode()));
                    
                    // Validate: Phải là mã ORDER
                    if (!Promotion.ApplicableFor.ORDER.name().equals(storeOrderPromotion.getApplicableFor())) {
                        throw new IllegalArgumentException("Mã " + storePromo.getOrderPromotionCode() + " không phải là mã giảm giá đơn hàng");
                    }
                    
                    // Validate điều kiện áp dụng (dùng currentTotal - giá sau platform discount)
                    if (isPromotionValid(storeOrderPromotion, currentTotal, store)) {
                        BigDecimal storeDiscount = calculateDiscount(currentTotal, storeOrderPromotion);
                        orderDiscount = orderDiscount.add(storeDiscount);
                        currentTotal = currentTotal.subtract(storeDiscount);
                        appliedPromotions.add(storeOrderPromotion);
                        
                        // Tăng usedCount
                        storeOrderPromotion.setUsedCount(storeOrderPromotion.getUsedCount() != null ? storeOrderPromotion.getUsedCount() + 1 : 1);
                        promotionRepository.save(storeOrderPromotion);
                    } else {
                        throw new IllegalArgumentException("Mã giảm giá đơn hàng không hợp lệ hoặc không đủ điều kiện cho cửa hàng: " + store.getName());
                    }
                }
            }
            
            // 10.3.3. Xử lý SHIPPING promotions
            BigDecimal shippingFee = BigDecimal.valueOf(30000); // Default shipping fee
            
            // Platform SHIPPING Promotion (áp dụng trước)
            if (platformShippingPromotion != null) {
                // Check minOrderValue với storeTotal gốc (không phải currentTotal)
                if (platformShippingPromotion.getMinOrderValue() == null || 
                    storeTotal.compareTo(BigDecimal.valueOf(platformShippingPromotion.getMinOrderValue())) >= 0) {
                    
                    BigDecimal platformShippingDiscount = calculateDiscount(shippingFee, platformShippingPromotion);
                    shippingDiscount = shippingDiscount.add(platformShippingDiscount);
                    appliedPromotions.add(platformShippingPromotion);
                }
            }
            
            // Store SHIPPING Promotion (áp dụng sau platform)
            if (orderDTO.getStorePromotions() != null && orderDTO.getStorePromotions().containsKey(storeId)) {
                OrderDTO.StorePromotions storePromo = orderDTO.getStorePromotions().get(storeId);
                
                if (storePromo.getShippingPromotionCode() != null && !storePromo.getShippingPromotionCode().trim().isEmpty()) {
                    Promotion storeShippingPromotion = promotionRepository.findByCode(storePromo.getShippingPromotionCode())
                            .orElseThrow(() -> new IllegalArgumentException("Mã giảm phí vận chuyển không tồn tại: " + storePromo.getShippingPromotionCode()));
                    
                    // Validate: Phải là mã SHIPPING
                    if (!Promotion.ApplicableFor.SHIPPING.name().equals(storeShippingPromotion.getApplicableFor())) {
                        throw new IllegalArgumentException("Mã " + storePromo.getShippingPromotionCode() + " không phải là mã giảm phí vận chuyển");
                    }
                    
                    // Validate điều kiện áp dụng (dùng storeTotal gốc để check min order)
                    if (isPromotionValid(storeShippingPromotion, storeTotal, store)) {
                        BigDecimal remainingShippingFee = shippingFee.subtract(shippingDiscount);
                        BigDecimal storeShippingDiscount = calculateDiscount(remainingShippingFee, storeShippingPromotion);
                        shippingDiscount = shippingDiscount.add(storeShippingDiscount);
                        appliedPromotions.add(storeShippingPromotion);
                        
                        // Tăng usedCount
                        storeShippingPromotion.setUsedCount(storeShippingPromotion.getUsedCount() != null ? storeShippingPromotion.getUsedCount() + 1 : 1);
                        promotionRepository.save(storeShippingPromotion);
                    } else {
                        throw new IllegalArgumentException("Mã giảm phí vận chuyển không hợp lệ hoặc không đủ điều kiện cho cửa hàng: " + store.getName());
                    }
                }
            }
            
            // 10.3.4. Tính tổng tiền cuối cùng
            BigDecimal finalShippingFee = shippingFee.subtract(shippingDiscount).max(BigDecimal.ZERO);
            BigDecimal finalTotal = storeTotal.subtract(orderDiscount).add(finalShippingFee).max(BigDecimal.ZERO);
    
            // 10.4. Tạo Order cho store này
            Order order = Order.builder()
                    .buyer(user)
                    .store(store)
                    .promotion(appliedPromotions.isEmpty() ? null : appliedPromotions.get(0)) // Lưu promotion đầu tiên (tạm thời)
                    .totalPrice(finalTotal)
                    .shippingFee(finalShippingFee)
                    .address(Address.builder()
                            .province(orderDTO.getAddress().getProvince())
                            .ward(orderDTO.getAddress().getWard())
                            .homeAddress(orderDTO.getAddress().getHomeAddress())
                            .phone(orderDTO.getAddress().getPhone())
                            .build()
                    )
                    .paymentMethod(orderDTO.getPaymentMethod())
                    .status(Order.OrderStatus.PENDING.name())
                    .note(orderDTO.getNote())
                    .build();
            
            order = orderRepository.save(order);
            
            // 7.5. Tạo OrderItems cho store này và trừ stock
            List<OrderItem> orderItems = new ArrayList<>();
            for (Cart.CartItemEmbedded cartItem : storeItems) {
                ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariant().getId()).get();
                
                // Trừ stock đúng cách (tổng stock hoặc stock theo màu)
                if (cartItem.getColorId() != null && productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
                    // Có màu sắc -> trừ stock của màu đó và cập nhật tổng stock
                    ProductVariant.ColorOption color = productVariant.getColors().stream()
                            .filter(c -> c.getId().equals(cartItem.getColorId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu sắc"));
                    
                    int newColorStock = color.getStock() - cartItem.getQuantity();
                    if (newColorStock < 0) {
                        throw new IllegalArgumentException("Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() + 
                                " (màu: " + cartItem.getColorId() + "), Số lượng còn lại: " + color.getStock() + 
                                ", Số lượng yêu cầu: " + cartItem.getQuantity());
                    }
                    
                    color.setStock(newColorStock);
                    
                    // Cập nhật tổng stock = tổng stock của tất cả màu
                    int totalStock = productVariant.getColors().stream().mapToInt(ProductVariant.ColorOption::getStock).sum();
                    productVariant.setStock(totalStock);
                } else {
                    // Không có màu sắc -> trừ stock tổng
                    int newStock = productVariant.getStock() - cartItem.getQuantity();
                    if (newStock < 0) {
                        throw new IllegalArgumentException("Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() + 
                                ", Số lượng còn lại: " + productVariant.getStock() + ", Số lượng yêu cầu: " + cartItem.getQuantity());
                    }
                    productVariant.setStock(newStock);
                }
                
                productVariantRepository.save(productVariant);
                
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productVariant(productVariant)
                        .quantity(cartItem.getQuantity())
                        .price(getProductPrice(productVariant, cartItem.getColorId()))
                        .colorId(cartItem.getColorId())
                        .build();
                
                orderItems.add(orderItem);
            }
            
            orderItemRepository.saveAll(orderItems);
            order.setOrderItems(orderItems);
            // 10.6. Add order into order list
            orders.add(order);
        }
        
        // 11. Tăng usedCount cho platform promotions (sau khi tạo tất cả orders thành công)
        if (platformOrderPromotion != null) {
            platformOrderPromotion.setUsedCount(
                platformOrderPromotion.getUsedCount() != null ? 
                platformOrderPromotion.getUsedCount() + orders.size() : orders.size()
            );
            promotionRepository.save(platformOrderPromotion);
        }
        
        if (platformShippingPromotion != null) {
            platformShippingPromotion.setUsedCount(
                platformShippingPromotion.getUsedCount() != null ? 
                platformShippingPromotion.getUsedCount() + orders.size() : orders.size()
            );
            promotionRepository.save(platformShippingPromotion);
        }
        
        // 12. Xóa các sản phẩm đã thanh toán khỏi giỏ hàng
        List<String> productVariantIds = new ArrayList<>();
        List<String> colorIds = new ArrayList<>();
        for (var item : selectedCartItems) {
            productVariantIds.add(item.getProductVariant().getId());
            colorIds.add(item.getColorId());
        }
        cartService.removeSelectedItems(user, productVariantIds, colorIds);
        
        // 13. Return danh sách orders
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

        for (Order order : orders.getContent()) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            order.setOrderItems(orderItems);
        }

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
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        order.setOrderItems(orderItems);

        return order;
    }
    
    @Override
    @Transactional
    public void cancelOrder(String userEmail, String orderId) throws Exception {
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
            
            // Hoàn trả stock đúng cách (tổng stock hoặc stock theo màu)
            if (item.getColorId() != null && productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
                // Có màu sắc -> hoàn trả stock cho màu đó và cập nhật tổng stock
                ProductVariant.ColorOption color = productVariant.getColors().stream()
                        .filter(c -> c.getId().equals(item.getColorId()))
                        .findFirst()
                        .orElse(null);
                
                if (color != null) {
                    color.setStock(color.getStock() + item.getQuantity());
                    
                    // Cập nhật tổng stock = tổng stock của tất cả màu
                    int totalStock = productVariant.getColors().stream().mapToInt(ProductVariant.ColorOption::getStock).sum();
                    productVariant.setStock(totalStock);
                }
            } else {
                // Không có màu sắc -> hoàn trả stock tổng
                productVariant.setStock(productVariant.getStock() + item.getQuantity());
            }
            
            productVariantRepository.save(productVariant);
        }
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
        // 1. Kiểm tra status
        if (!Promotion.PromotionStatus.ACTIVE.name().equals(promotion.getStatus())) {
            return false;
        }
        
        // 2. Kiểm tra thời gian
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promotion.getStartDate()) || now.isAfter(promotion.getEndDate())) {
            return false;
        }
        
        // 3. Kiểm tra giá trị đơn hàng tối thiểu
        if (promotion.getMinOrderValue() != null && 
            orderTotal.compareTo(BigDecimal.valueOf(promotion.getMinOrderValue())) < 0) {
            return false;
        }
        
        // 4. Kiểm tra usage limit
        if (promotion.getUsageLimit() != null && 
            promotion.getUsedCount() >= promotion.getUsageLimit()) {
            return false;
        }
        
        // 5. Kiểm tra issuer (Shop hay Platform)
        if (Promotion.Issuer.STORE.name().equals(promotion.getIssuer())) {
            // Nếu là mã của Shop -> phải cùng store
            if (promotion.getStore() == null || 
                !promotion.getStore().getId().equals(store.getId())) {
                return false;
            }
        }
        // Nếu là PLATFORM -> áp dụng cho tất cả shop
        
        return true;
    }
    
    /**
     * Apply promotion discount to order total
     */
    private BigDecimal calculateDiscount(BigDecimal amount, Promotion promotion) {
        if (Promotion.PromotionType.PERCENTAGE.name().equals(promotion.getType())) {
            // Giảm theo phần trăm
            BigDecimal discount = amount
                .multiply(BigDecimal.valueOf(promotion.getDiscountValue()))
                .divide(BigDecimal.valueOf(100));
            
            // Áp dụng maxDiscountValue nếu có
            if (promotion.getMaxDiscountValue() != null) {
                BigDecimal maxDiscount = BigDecimal.valueOf(promotion.getMaxDiscountValue());
                discount = discount.min(maxDiscount);
            }
            
            return discount;
        } else {
            // Giảm số tiền cố định
            BigDecimal discount = BigDecimal.valueOf(promotion.getDiscountValue());
            return discount.min(amount); // Không giảm quá số tiền gốc
        }
    }
    
    // ===== SELLER METHODS =====
    
    @Override
    public Page<Order> getStoreOrders(String storeId, int page, int size, String status) throws Exception {
        // Tạo Pageable (page bắt đầu từ 0)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Order> orders;
        if (status != null && !status.trim().isEmpty()) {
            orders = orderRepository.findByStoreIdAndStatus(storeId, status, pageable);
        } else {
            orders = orderRepository.findByStoreId(storeId, pageable);
        }
        
        // Load orderItems cho mỗi order
        for (Order order : orders.getContent()) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            order.setOrderItems(orderItems);
        }
        
        return orders;
    }
    
    @Override
    public Order getStoreOrderDetail(String storeId, String orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        // Kiểm tra order có thuộc về store này không
        if (!order.getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException("Đơn hàng không thuộc về cửa hàng này");
        }
        
        // Load orderItems
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        order.setOrderItems(orderItems);
        
        return order;
    }
    
    @Override
    public Order updateOrderStatus(String storeId, String orderId, String newStatus) throws Exception {
        Order order = getStoreOrderDetail(storeId, orderId);
        
        // Kiểm tra trạng thái hợp lệ
        String currentStatus = order.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                String.format("Không thể chuyển từ trạng thái %s sang %s", currentStatus, newStatus)
            );
        }
        
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
    
    @Override
    public Map<String, Object> getStoreOrderStatistics(String storeId) throws Exception {
        Map<String, Object> stats = new HashMap<>();
        
        // Tổng số đơn hàng
        long totalOrders = orderRepository.countByStoreId(storeId);
        stats.put("totalOrders", totalOrders);
        
        // Đếm theo trạng thái
        stats.put("pending", orderRepository.countByStoreIdAndStatus(storeId, "PENDING"));
        stats.put("confirmed", orderRepository.countByStoreIdAndStatus(storeId, "CONFIRMED"));
        stats.put("shipping", orderRepository.countByStoreIdAndStatus(storeId, "SHIPPING"));
        stats.put("delivered", orderRepository.countByStoreIdAndStatus(storeId, "DELIVERED"));
        stats.put("cancelled", orderRepository.countByStoreIdAndStatus(storeId, "CANCELLED"));
        
        // Tính tổng doanh thu (chỉ đơn hàng hoàn thành)
        List<Order> completedOrders = orderRepository.findByStoreIdAndStatus(storeId, "DELIVERED");
        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getStoreRevenue(String storeId, String startDate, String endDate) throws Exception {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, start, end);
        
        Map<String, Object> revenue = new HashMap<>();
        
        // Lọc theo trạng thái và tính doanh thu
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal pendingRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        long completedOrders = 0;
        
        for (Order order : orders) {
            totalOrders++;
            if ("DELIVERED".equals(order.getStatus())) {
                totalRevenue = totalRevenue.add(order.getTotalPrice());
                completedOrders++;
            } else if (!"CANCELLED".equals(order.getStatus())) {
                pendingRevenue = pendingRevenue.add(order.getTotalPrice());
            }
        }
        
        revenue.put("totalRevenue", totalRevenue);
        revenue.put("pendingRevenue", pendingRevenue);
        revenue.put("totalOrders", totalOrders);
        revenue.put("completedOrders", completedOrders);
        revenue.put("startDate", startDate);
        revenue.put("endDate", endDate);
        
        return revenue;
    }
    
    /**
     * Kiểm tra việc chuyển trạng thái có hợp lệ không
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "PENDING":
                return "CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "CONFIRMED":
                return "SHIPPING".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "SHIPPING":
                return "DELIVERED".equals(newStatus);
            case "DELIVERED":
            case "CANCELLED":
                return false; // Không thể thay đổi trạng thái cuối
            default:
                return false;
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