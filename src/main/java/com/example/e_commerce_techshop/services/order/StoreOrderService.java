package com.example.e_commerce_techshop.services.order;

import com.example.e_commerce_techshop.dtos.b2c.order.OrderDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.models.*;
import com.example.e_commerce_techshop.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreOrderService implements IStoreOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;

    @Override
    public List<OrderDTO> getOrdersByStore(String storeId) throws Exception {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByStoreAndStatus(String storeId, String status) throws Exception {
        List<Order> orders = orderRepository.findByStoreIdAndStatus(storeId, status);
        return orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getRecentOrdersByStore(String storeId, int limit) throws Exception {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        return orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(String orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        return convertToOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(String orderId, OrderDTO updateDTO) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        if (!isValidStatusTransition(order.getStatus(), updateDTO.getStatus())) {
            throw new IllegalArgumentException("Không thể chuyển từ trạng thái " + order.getStatus() + " sang " + updateDTO.getStatus());
        }
        order.setStatus(updateDTO.getStatus());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        return convertToOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(String orderId, String reason) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc CONFIRMED");
        }
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        for (OrderItem item : order.getOrderItems()) {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId()).get();
            productVariant.setStock(productVariant.getStock() + item.getQuantity());
            productVariantRepository.save(productVariant);
        }
        order = orderRepository.save(order);
        return convertToOrderDTO(order);
    }

    @Override
    public Map<String, Long> getOrderStatistics(String storeId) throws Exception {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", orderRepository.countByStoreId(storeId));
        statistics.put("pending", orderRepository.countByStoreIdAndStatus(storeId, "PENDING"));
        statistics.put("confirmed", orderRepository.countByStoreIdAndStatus(storeId, "CONFIRMED"));
        statistics.put("processing", orderRepository.countByStoreIdAndStatus(storeId, "PROCESSING"));
        statistics.put("shipped", orderRepository.countByStoreIdAndStatus(storeId, "SHIPPED"));
        statistics.put("delivered", orderRepository.countByStoreIdAndStatus(storeId, "DELIVERED"));
        statistics.put("cancelled", orderRepository.countByStoreIdAndStatus(storeId, "CANCELLED"));
        return statistics;
    }

    @Override
    public List<OrderDTO> getOrdersByDateRange(String storeId, String startDate, String endDate) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, start, end);
        return orders.stream().map(this::convertToOrderDTO).collect(Collectors.toList());
    }

    @Override
    public Page<OrderDTO> getOrdersByStoreWithPagination(String storeId, int page, int size, String status) throws Exception {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> ordersPage = (status != null && !status.equals("ALL"))
                ? orderRepository.findByStoreIdAndStatus(storeId, status, pageable)
                : orderRepository.findByStoreId(storeId, pageable);
        return ordersPage.map(this::convertToOrderDTO);
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        Map<String, List<String>> validTransitions = Map.of(
                "PENDING", List.of("CONFIRMED", "CANCELLED"),
                "CONFIRMED", List.of("PROCESSING", "CANCELLED"),
                "PROCESSING", List.of("SHIPPED", "CANCELLED"),
                "SHIPPED", List.of("DELIVERED"),
                "DELIVERED", List.of(),
                "CANCELLED", List.of()
        );
        return validTransitions.getOrDefault(currentStatus, List.of()).contains(newStatus);
    }

    private OrderDTO convertToOrderDTO(Order order) {
        User buyer = userRepository.findById(order.getBuyerId()).orElse(null);
        String buyerName = buyer != null ? buyer.getFullName() : "Unknown Buyer";
        String buyerEmail = buyer != null ? buyer.getEmail() : "Unknown Email";
        String buyerPhone = buyer != null ? buyer.getPhone() : "Unknown Phone";

        List<OrderDTO.OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId())
                .stream().map(this::convertToOrderItemDTO).collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .buyerName(buyerName)
                .buyerEmail(buyerEmail)
                .buyerPhone(buyerPhone)
                .storeId(order.getStoreId())
                .promotionId(order.getPromotionId())
                .totalPrice(order.getTotalPrice())
                .addressId(order.getAddressId())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItems)
                .build();
    }


    private OrderDTO.OrderItem convertToOrderItemDTO(OrderItem orderItem) {
        ProductVariant productVariant = productVariantRepository.findById(orderItem.getProductVariantId()).orElse(null);
        String productName = "Unknown Product";
        String variantName = "Unknown Variant";
        String productImage = null;
        String productId = null;
        String category = null;
        String brand = null;
        Integer stock = 0;
        if (productVariant != null) {
            productName = productVariant.getName();
            variantName = productVariant.getName();
            productImage = productVariant.getImageUrl();
            stock = productVariant.getStock();
            if (productVariant.getProduct() != null) {
                productId = productVariant.getProduct().getId();
                category = productVariant.getProduct().getCategory();
                if (productVariant.getProduct().getBrand() != null) {
                    brand = productVariant.getProduct().getBrand().getName();
                }
            }
        }
        return OrderDTO.OrderItem.builder()
                .id(orderItem.getId())
                .productVariantId(orderItem.getProductVariantId())
                .productName(productName)
                .variantName(variantName)
                .productImage(productImage)
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .subtotal(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .productId(productId)
                .category(category)
                .brand(brand)
                .stock(stock)
                .build();
    }
}


