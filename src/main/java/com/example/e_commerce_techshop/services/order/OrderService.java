package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.b2c.order.OrderDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.*;
import com.example.e_commerce_techshop.repositories.*;
import com.example.e_commerce_techshop.responses.OrderResponse;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final UserRepository userRepository;

    private final AddressRepository addressRepository;
    
    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final PromotionRepository promotionRepository;

    private final ProductVariantRepository productVariantRepository;

    private final INotificationService notificationService;
    
    @Override
    @Transactional
    public void createOrder(OrderDTO orderDTO) throws Exception {
        // B1: Lấy giỏ hàng
        Cart cart = cartRepository.findByUserId(orderDTO.getBuyerId());
        if(cart == null)
            throw new RuntimeException("Giỏ hàng của người dùng không được tìm thấy");
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("Giỏ hàng không có sản phẩm");
        }

        // Bước 2: Nhóm card_items theo store_id
        Map<String, List<CartItem>> itemsByStore = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProductVariant().getProduct().getStore().getId()
                ));
        List<Order> createdOrders = new ArrayList<>();

        // Bước 3: Tạo order cho từng store
        for (Map.Entry<String, List<CartItem>> entry : itemsByStore.entrySet()) {
            String storeId = entry.getKey();
            List<CartItem> storeItems = entry.getValue();

            // Kiểm tra stock
            for (CartItem item : storeItems) {
                ProductVariant variant = item.getProductVariant();
                if (variant.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Không còn đủ sản phẩm cho" + variant.getName() + " mà bạn yêu cầu");
                }
            }

            // Lấy promotion (nếu có) cho store này từ promotionIds
            String promotionId = orderDTO.getPromotionIds() != null ? orderDTO.getPromotionIds().get(storeId) : null;
            Promotion promotion = null;
            if (promotionId != null) {
                promotion = promotionRepository.findByIdAndStoreId(promotionId, storeId)
                        .orElseThrow(() -> new RuntimeException("Promotion " + promotionId + " is not valid for store " + storeId));
            }

            // Tính tổng giá (áp dụng promotion nếu có)
            BigDecimal totalPrice = calculateTotalPrice(storeItems, promotion);

            // Tạo order
            Order order = Order.builder()
                    .buyer(cart.getUser())
                    .store(storeItems.get(0).getProductVariant().getProduct().getStore())
                    .promotion(promotion)
                    .totalPrice(totalPrice)
                    .address(addressRepository.findById(orderDTO.getAddressId())
                            .orElseThrow(() -> new DataNotFoundException("Không tìm thấy địa chỉ được cung cấp")))
                    .paymentMethod(orderDTO.getPaymentMethod())
                    .status("PENDING")
                    .build();
            orderRepository.save(order);

            // Tạo order_items
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : storeItems) {
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productVariant(item.getProductVariant())
                        .quantity(item.getQuantity())
                        .price(item.getProductVariant().getPrice())
                        .build();
                orderItems.add(orderItem);

                // Giảm stock
                ProductVariant variant = item.getProductVariant();
                variant.setStock(variant.getStock() - item.getQuantity());
                productVariantRepository.save(variant);
            }
            orderItemRepository.saveAll(orderItems);
            order.setOrderItems(orderItems);
            createdOrders.add(order);

            // Tạo notification cho buyer và seller
            notificationService.createNotification(orderDTO.getBuyerId(), "Đơn hàng đã được tạo",
                    "Đơn hàng " + order.getId() + " của cùa hàng " + order.getStore().getName() + " đã được đặt.");
            notificationService.createNotification(order.getStore().getOwner().getId(), "Đơn hàng mới",
                    "1 đơn hàng mới " + order.getId() + " được đặt trên cửa hàng của bạn.");
        }

    }

    private BigDecimal calculateTotalPrice(List<CartItem> items, Promotion promotion) {
        BigDecimal subtotal = items.stream()
                .map(item -> BigDecimal.valueOf(item.getQuantity())
                        .multiply(BigDecimal.valueOf(item.getProductVariant().getPrice())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (promotion != null && subtotal.compareTo(BigDecimal.valueOf(promotion.getMinOrderValue())) >= 0
                && "ACTIVE".equals(promotion.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(promotion.getStartDate()) && now.isBefore(promotion.getEndDate())) {
                if ("PERCENTAGE".equals(promotion.getDiscountType())) {
                    BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(promotion.getDiscountValue()))
                            .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
                    if (promotion.getMaxDiscountValue() > 0 && discount.compareTo(BigDecimal.valueOf(promotion.getMaxDiscountValue())) > 0) {
                        discount = BigDecimal.valueOf(promotion.getMaxDiscountValue());
                    }
                    subtotal = subtotal.subtract(discount);
                } else {
                    BigDecimal discount = BigDecimal.valueOf(Math.min(promotion.getDiscountValue(), promotion.getMaxDiscountValue()));
                    subtotal = subtotal.subtract(discount);
                }
            }
        }
        return subtotal.max(BigDecimal.ZERO);
    }
    
    @Override
    public OrderResponse updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(updatedOrder);
    }
    
    @Override
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        return OrderResponse.fromOrder(order);
    }
    
    @Override
    public List<OrderResponse> getOrdersByStore(String storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        return orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrderResponse> getOrdersByStoreAndStatus(String storeId, String status) {
        List<Order> orders = orderRepository.findByStoreIdAndStatus(storeId, status);
        return orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrderResponse> getRecentOrdersByStore(String storeId, int limit) {
        List<Order> orders = orderRepository.findRecentOrdersByStoreId(storeId);
        return orders.stream()
                .limit(limit)
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    @Override
    public OrderResponse cancelOrder(String orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        
        order.setStatus("CANCELLED");
        Order updatedOrder = orderRepository.save(order);
        return OrderResponse.fromOrder(updatedOrder);
    }
    
    @Override
    public Long getOrderCountByStoreAndStatus(String storeId, String status) {
        return orderRepository.countByStoreIdAndStatus(storeId, status);
    }
    
    @Override
    public List<OrderResponse> getOrdersByDateRange(String storeId, String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, start, end);
        return orders.stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
}



