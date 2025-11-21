package com.example.e_commerce_techshop.services.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.exceptions.InvalidPromotionException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.OrderItem;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.repositories.CartRepository;
import com.example.e_commerce_techshop.repositories.OrderItemRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.repositories.PromotionRepository;
import com.example.e_commerce_techshop.repositories.PromotionUsageRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.services.cart.ICartService;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;
import com.example.e_commerce_techshop.services.shipment.IShipmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PromotionRepository promotionRepository;
    private final StoreRepository storeRepository;
    private final CartRepository cartRepository;
    private final ICartService cartService;
    private final PromotionUsageRepository promotionUsageRepository;
    private final IPromotionService promotionService;
    private final INotificationService notificationService;
    private final IShipmentService shipmentService;

    @Override
    @Transactional
    public List<Order> checkout(User user, OrderDTO orderDTO) throws Exception {
        BigDecimal totalOrderValue = BigDecimal.ZERO;

        // 2. Validate payment method
        if (!isValidPaymentMethod(orderDTO.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ: " + orderDTO.getPaymentMethod());
        }

        // 3. Validate danh sách sản phẩm được chọn
        if (orderDTO.getSelectedItems() == null || orderDTO.getSelectedItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một sản phẩm để thanh toán");
        }

        // 4. Lấy cart hiện tại (để validation)
        cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giỏ hàng"));

        // 5. Validate từng sản phẩm được chọn
        // Biến lưu trữ các sản phẩm theo cửa hàng
        Map<String, List<OrderDTO.SelectedCartItem>> itemsByStore = new HashMap<>();

        for (var cartItem : orderDTO.getSelectedItems()) {
            ProductVariant productVariant = productVariantRepository.findById(cartItem.getProductVariantId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Không tìm thấy sản phẩm: " + cartItem.getProductVariantId()));

            if (cartItem.getColorId() != null && !cartItem.getColorId().trim().isEmpty()) {
                ColorOption colorOption = ProductVariant.getColor(productVariant, cartItem.getColorId());
                if (colorOption == null) {
                    throw new IllegalArgumentException("Sản phẩm không có màu sắc như yêu cầu");
                }
                if (cartItem.getQuantity() > colorOption.getStock()) {
                    throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ. Không đủ hàng trong kho");
                }
                totalOrderValue = totalOrderValue.add(
                        BigDecimal.valueOf(colorOption.getPrice())
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            } else {
                if (cartItem.getQuantity() > productVariant.getStock()) {
                    throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ. Không đủ hàng trong kho");
                }
                totalOrderValue = totalOrderValue.add(
                        BigDecimal.valueOf(productVariant.getPrice())
                                .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            }

            // Kiểm tra trạng thái store
            if (!Store.StoreStatus.APPROVED.name().equals(productVariant.getProduct().getStore().getStatus())) {
                throw new IllegalArgumentException(
                        "Cửa hàng tạm thời đóng cửa: " + productVariant.getProduct().getStore().getName());
            }

            String storeId = productVariant.getProduct().getStore().getId();
            itemsByStore.computeIfAbsent(storeId, k -> new ArrayList<>()).add(cartItem);
        }

        // 6. Validate và xử lý Platform Promotions
        Promotion platformOrderPromotion = null;
        Promotion platformShippingPromotion = null;

        // 7. Tính số orders sẽ được tạo ra
        int numberOfOrders = itemsByStore.size();

        if (orderDTO.getPlatformPromotions() != null) {
            // 7.1. Platform ORDER Promotion - Validate
            if (orderDTO.getPlatformPromotions().getOrderPromotionCode() != null &&
                    !orderDTO.getPlatformPromotions().getOrderPromotionCode().trim().isEmpty()) {

                String code = orderDTO.getPlatformPromotions().getOrderPromotionCode();
                platformOrderPromotion = promotionRepository.findByCode(code)
                        .orElseThrow(() -> new DataNotFoundException("Mã giảm giá sàn không tồn tại: " + code));

                // Validate: Phải là PLATFORM và ORDER
                if (!Promotion.Issuer.PLATFORM.name().equals(platformOrderPromotion.getIssuer())) {
                    throw new IllegalArgumentException("Mã " + code + " không phải là mã của sàn");
                }
                if (!Promotion.ApplicableFor.ORDER.name().equals(platformOrderPromotion.getApplicableFor())) {
                    throw new IllegalArgumentException("Mã " + code + " không phải là mã giảm giá đơn hàng");
                }

                // Validate với user (isNewUserOnly, usageLimitPerUser)
                try {
                    promotionService.validatePromotionForUser(
                            platformOrderPromotion,
                            totalOrderValue.longValue(),
                            user);

                } catch (InvalidPromotionException e) {
                    throw new InvalidPromotionException(
                            "Không thể áp dụng mã giảm giá sàn cho đơn hàng. " + e.getMessage());
                }
                // 7.2. Platform SHIPPING Promotion - Validate
                if (orderDTO.getPlatformPromotions().getShippingPromotionCode() != null &&
                        !orderDTO.getPlatformPromotions().getShippingPromotionCode().trim().isEmpty()) {

                    String codePlatform = orderDTO.getPlatformPromotions().getShippingPromotionCode();
                    platformShippingPromotion = promotionRepository.findByCode(codePlatform)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Mã giảm phí ship sàn không tồn tại: " + codePlatform));

                    // Validate: Phải là PLATFORM và SHIPPING
                    if (!Promotion.Issuer.PLATFORM.name().equals(platformShippingPromotion.getIssuer())) {
                        throw new IllegalArgumentException("Mã " + codePlatform + " không phải là mã của sàn");
                    }
                    if (!Promotion.ApplicableFor.SHIPPING.name().equals(platformShippingPromotion.getApplicableFor())) {
                        throw new IllegalArgumentException(
                                "Mã " + codePlatform + " không phải là mã giảm phí vận chuyển");
                    }

                    // Validate với user
                    try {
                        promotionService.validatePromotionForUser(
                                platformShippingPromotion,
                                totalOrderValue.longValue(),
                                user);

                    } catch (InvalidPromotionException e) {
                        throw new InvalidPromotionException(
                                "Không thể áp dụng mã giảm phí ship sàn. " + e.getMessage());
                    }
                }
            }
        }

        // 8. Lấy danh sách store áp dụng shipping voucher
        List<String> applyShippingToStores = new ArrayList<>();
        if (platformShippingPromotion != null) {
            if (orderDTO.getPlatformPromotions().getApplyShippingToStores() != null &&
                    !orderDTO.getPlatformPromotions().getApplyShippingToStores().isEmpty()) {
                applyShippingToStores = orderDTO.getPlatformPromotions().getApplyShippingToStores();

                // Validate usage limit với số đơn muốn áp dụng
                if (platformShippingPromotion.getUsageLimit() != null) {
                    int remainingUsage = platformShippingPromotion.getUsageLimit()
                            - platformShippingPromotion.getUsedCount();
                    if (remainingUsage < applyShippingToStores.size()) {
                        throw new IllegalArgumentException(
                                String.format(
                                        "Mã giảm phí ship sàn chỉ còn %d lượt sử dụng nhưng bạn muốn áp dụng cho %d đơn hàng",
                                        remainingUsage, applyShippingToStores.size()));
                    }
                }
            } else {
                // Nếu không chỉ định, áp dụng cho tất cả stores
                if (platformShippingPromotion.getUsageLimit() != null) {
                    int remainingUsage = platformShippingPromotion.getUsageLimit()
                            - platformShippingPromotion.getUsedCount();
                    if (remainingUsage < numberOfOrders) {
                        throw new IllegalArgumentException(
                                String.format(
                                        "Mã giảm phí ship sàn chỉ còn %d lượt sử dụng nhưng bạn muốn áp dụng cho %d đơn hàng",
                                        remainingUsage, numberOfOrders));
                    }
                }
                applyShippingToStores = new ArrayList<>(itemsByStore.keySet());
            }
        }

        // 9. Sắp xếp stores theo tổng giá trị giảm dần để ưu tiên áp dụng platform
        // order voucher
        List<Map.Entry<String, List<OrderDTO.SelectedCartItem>>> sortedStores = new ArrayList<>(
                itemsByStore.entrySet());
        sortedStores.sort((e1, e2) -> {
            BigDecimal total1 = calculateStoreTotal(e1.getValue());
            BigDecimal total2 = calculateStoreTotal(e2.getValue());
            return total2.compareTo(total1); // Giảm dần
        });

        List<Order> orders = new ArrayList<>();
        int platformOrderPromotionUsed = 0; // Đếm số lần đã dùng platform order voucher

        // 10. Tạo 1 đơn hàng cho mỗi store (theo thứ tự đã sắp xếp)
        for (Map.Entry<String, List<OrderDTO.SelectedCartItem>> storeEntry : sortedStores) {
            String storeId = storeEntry.getKey();
            List<OrderDTO.SelectedCartItem> storeItems = storeEntry.getValue();

            // 10.1. Lấy thông tin store
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng với ID: " + storeId));

            // 10.2. Tính tổng tiền cho store này
            BigDecimal storeTotal = BigDecimal.ZERO;
            for (OrderDTO.SelectedCartItem item : storeItems) {
                ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId()).get();
                Long itemPrice = getProductPrice(productVariant, item.getColorId());
                BigDecimal itemTotal = BigDecimal.valueOf(itemPrice)
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                storeTotal = storeTotal.add(itemTotal);
            }

            // 10.3. Xử lý promotion - Thứ tự: Store ORDER -> Platform ORDER -> Platform
            // Shipping
            List<Promotion> appliedPromotions = new ArrayList<>();
            BigDecimal storeDiscountAmount = BigDecimal.ZERO; // Tiền shop chịu
            BigDecimal platformDiscountAmount = BigDecimal.ZERO; // Tiền sàn chịu
            BigDecimal shippingDiscount = BigDecimal.ZERO;

            BigDecimal currentTotal = storeTotal; // Track giá trị sau mỗi lần discount

            // 10.3.1. Store ORDER Promotion (Áp dụng TRƯỚC)
            if (orderDTO.getStorePromotions() != null && orderDTO.getStorePromotions().containsKey(storeId)) {
                String storePromo = orderDTO.getStorePromotions().get(storeId);

                if (storePromo != null && !storePromo.trim().isEmpty()) {
                    Promotion storeOrderPromotion = promotionRepository.findByCode(storePromo)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Mã giảm giá đơn hàng không tồn tại: " + storePromo));

                    // Validate: Phải là mã ORDER
                    if (!Promotion.ApplicableFor.ORDER.name().equals(storeOrderPromotion.getApplicableFor())) {
                        throw new IllegalArgumentException("Mã " + storePromo + " không phải là mã giảm giá đơn hàng");
                    }

                    // Validate điều kiện áp dụng
                    if (isPromotionValid(storeOrderPromotion, currentTotal, user, store)) {
                        BigDecimal storeDiscount = calculateDiscount(currentTotal, storeOrderPromotion);
                        storeDiscountAmount = storeDiscountAmount.add(storeDiscount); // Shop chịu chi phí này
                        currentTotal = currentTotal.subtract(storeDiscount);
                        appliedPromotions.add(storeOrderPromotion);
                    } else {
                        throw new IllegalArgumentException(
                                "Mã giảm giá đơn hàng không hợp lệ hoặc không đủ điều kiện cho cửa hàng: "
                                        + store.getName());
                    }
                }
            }

            // 10.3.2. Platform ORDER Promotion (Áp dụng SAU store voucher)
            // Ưu tiên áp dụng cho đơn có giá trị cao hơn cho đến khi hết lượt
            if (platformOrderPromotion != null) {
                int remainingUsage = platformOrderPromotion.getUsageLimit() != null
                        ? platformOrderPromotion.getUsageLimit()
                                - (platformOrderPromotion.getUsedCount() + platformOrderPromotionUsed)
                        : Integer.MAX_VALUE;

                if (remainingUsage > 0) {
                    // Check minOrderValue với currentTotal (sau khi áp dụng store voucher)
                    if (platformOrderPromotion.getMinOrderValue() == null ||
                            currentTotal
                                    .compareTo(BigDecimal.valueOf(platformOrderPromotion.getMinOrderValue())) >= 0) {

                        BigDecimal platformDiscount = calculateDiscount(currentTotal, platformOrderPromotion);
                        platformDiscountAmount = platformDiscountAmount.add(platformDiscount); // Sàn chịu chi phí này
                        currentTotal = currentTotal.subtract(platformDiscount);
                        appliedPromotions.add(platformOrderPromotion);
                        platformOrderPromotionUsed++; // Tăng số lần đã dùng
                    }
                }
            }

            // 10.3.3. Platform SHIPPING Promotion (tính riêng)
            BigDecimal shippingFee = BigDecimal.valueOf(30000); // Default shipping fee

            // Chỉ áp dụng nếu store này nằm trong danh sách được chọn
            if (platformShippingPromotion != null && applyShippingToStores.contains(storeId)) {
                // Check minOrderValue với storeTotal gốc (không phải currentTotal)
                if (platformShippingPromotion.getMinOrderValue() == null ||
                        storeTotal.compareTo(BigDecimal.valueOf(platformShippingPromotion.getMinOrderValue())) >= 0) {

                    BigDecimal platformShippingDiscount = calculateDiscount(shippingFee, platformShippingPromotion);
                    shippingDiscount = shippingDiscount.add(platformShippingDiscount);
                    appliedPromotions.add(platformShippingPromotion);
                }
            }

            // 10.3.4. Tính tổng tiền cuối cùng
            BigDecimal finalShippingFee = shippingFee.subtract(shippingDiscount).max(BigDecimal.ZERO);

            // 10.3.5. Tính phí dịch vụ (cố định: 5000đ cho mỗi đơn hàng)
            BigDecimal serviceFee = BigDecimal.valueOf(5000);

            // 10.3.6. Tính tổng tiền cuối cùng khách hàng phải thanh toán
            BigDecimal totalDiscount = storeDiscountAmount.add(platformDiscountAmount);
            BigDecimal finalTotal = storeTotal.subtract(totalDiscount).add(finalShippingFee).max(BigDecimal.ZERO);

            // 10.4. Tạo Order cho store này
            Order order = Order.builder()
                    .buyer(user)
                    .store(store)
                    .promotions(appliedPromotions.isEmpty() ? null : appliedPromotions)
                    .productPrice(storeTotal) // Giá sản phẩm
                    .shippingFee(finalShippingFee) // Phí ship
                    .serviceFee(serviceFee) // Phí dịch vụ
                    .storeDiscountAmount(storeDiscountAmount) // Tiền shop chịu
                    .platformDiscountAmount(platformDiscountAmount) // Tiền sàn chịu
                    .totalDiscountAmount(platformDiscountAmount)
                    .totalPrice(finalTotal) // Tổng tiền khách thanh toán
                    .isRated(false)
                    .vnpTnxRef(orderDTO.getVnpTnxRef())
                    .address(Address.builder()
                            .province(orderDTO.getAddress().getProvince())
                            .ward(orderDTO.getAddress().getWard())
                            .homeAddress(orderDTO.getAddress().getHomeAddress())
                            .phone(orderDTO.getAddress().getPhone())
                            .build())
                    .paymentMethod(orderDTO.getPaymentMethod())
                    .status(Order.OrderStatus.PENDING.name())
                    .note(orderDTO.getNote())
                    .build();

            order = orderRepository.save(order);

            // 10.5. Tạo OrderItems cho store này và trừ stock
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderDTO.SelectedCartItem item : storeItems) {
                ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId()).get();

                // Trừ stock đúng cách (tổng stock hoặc stock theo màu)
                if (item.getColorId() != null && productVariant.getColors() != null
                        && !productVariant.getColors().isEmpty()) {
                    // Có màu sắc -> trừ stock của màu đó và cập nhật tổng stock
                    ProductVariant.ColorOption color = productVariant.getColors().stream()
                            .filter(c -> c.getId().equals(item.getColorId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu sắc"));

                    int newColorStock = color.getStock() - item.getQuantity();
                    if (newColorStock < 0) {
                        throw new IllegalArgumentException(
                                "Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() +
                                        " (màu: " + item.getColorId() + "), Số lượng còn lại: " + color.getStock() +
                                        ", Số lượng yêu cầu: " + item.getQuantity());
                    }

                    color.setStock(newColorStock);

                    // Cập nhật tổng stock = tổng stock của tất cả màu
                    int totalStock = productVariant.getColors().stream().mapToInt(ProductVariant.ColorOption::getStock)
                            .sum();
                    productVariant.setStock(totalStock);
                } else {
                    // Không có màu sắc -> trừ stock tổng
                    int newStock = productVariant.getStock() - item.getQuantity();
                    if (newStock < 0) {
                        throw new IllegalArgumentException(
                                "Không đủ hàng trong kho. Sản phẩm: " + productVariant.getName() +
                                        ", Số lượng còn lại: " + productVariant.getStock() + ", Số lượng yêu cầu: "
                                        + item.getQuantity());
                    }
                    productVariant.setStock(newStock);
                }

                productVariantRepository.save(productVariant);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productVariant(productVariant)
                        .quantity(item.getQuantity())
                        .price(getProductPrice(productVariant, item.getColorId()))
                        .colorId(item.getColorId())
                        .build();

                orderItems.add(orderItem);
            }

            orderItemRepository.saveAll(orderItems);
            order.setOrderItems(orderItems);

            // 10.6. Ghi nhận sử dụng promotion
            for (Promotion promotion : appliedPromotions) {
                try {
                    promotionService.recordPromotionUsage(promotion, user);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to record promotion usage: " + e.getMessage());
                }
            }

            // 10.7. Add order into order list
            orders.add(order);
        }

        List<String> selectedCartItemIds = orderDTO.getSelectedItems().stream()
                .map(OrderDTO.SelectedCartItem::getId)
                .collect(Collectors.toList());

        cartService.removeSelectedItemsByIds(user, selectedCartItemIds);

        // 12. Gửi thông báo cho người mua và người bán
        for (Order order : orders) {
            try {
                // Thông báo cho người mua
                notificationService.createUserNotification(user.getId(),
                        "Đơn hàng mới được tạo",
                        String.format("Đơn hàng #%s của bạn đã được tạo thành công. Tổng tiền: %,.0f đ",
                                order.getId(), order.getTotalPrice().doubleValue()));

                // Thông báo cho chủ shop (store)
                // Giá gửi cho shop = productPrice - storeDiscountAmount (chỉ discount từ mã của shop) - serviceFee
                BigDecimal storePrice = order.getProductPrice().subtract(order.getStoreDiscountAmount() != null
                        ? order.getStoreDiscountAmount()
                        : BigDecimal.ZERO)
                        .subtract(order.getServiceFee() != null ? order.getServiceFee() : BigDecimal.ZERO);
                notificationService.createStoreNotification(order.getStore().getId(),
                        "Có đơn hàng mới",
                        String.format("Bạn nhận được đơn hàng mới #%s từ khách %s. Giá: %,.0f đ",
                                order.getId(), user.getFullName(), storePrice.doubleValue()));
            } catch (Exception e) {
                System.err.println("Error creating notification: " + e.getMessage());
            }
        }

        // 13. Return danh sách orders
        return orders;

    }

    @Override
    public Page<Order> getOrderHistory(User user, String status, Pageable pageable) throws Exception {
        Page<Order> orderPage;
        if (status == null || status.isEmpty()) {
            orderPage = orderRepository.findByBuyerId(user.getId(), pageable);
        } else {
            orderPage = orderRepository.findByBuyerIdAndStatus(user.getId(), status, pageable);
        }
        if (!orderPage.getContent().isEmpty()) {
            List<String> orderIds = orderPage.getContent().stream()
                    .map(Order::getId)
                    .toList();

            List<OrderItem> allOrderItems = orderItemRepository.findByOrderIdIn(orderIds);

            // Group orderItems by orderId
            Map<String, List<OrderItem>> itemsByOrderId = allOrderItems.stream()
                    .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

            // Assign orderItems to each order
            orderPage.getContent().forEach(order -> {
                List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>());
                order.setOrderItems(items);
            });
        }
        return orderPage;
    }

    @Override
    public Order getOrderDetail(User user, String orderId) throws Exception {
        // 1. Convert email to User object
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
    public void cancelOrder(User user, String orderId) throws Exception {
        String buyerId = user.getId();

        // 2. Tìm order
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));

        // 3. Kiểm tra có thể hủy không
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể hủy đơn hàng ở trạng thái PENDING");
        }

        order.setStatus(Order.OrderStatus.CANCELLED.name());

        // 4. Hoàn trả stock
        List<OrderItem> orderItem = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : orderItem) {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariant().getId())
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy sản phẩm"));

            // Hoàn trả stock đúng cách (tổng stock hoặc stock theo màu)
            if (item.getColorId() != null && productVariant.getColors() != null
                    && !productVariant.getColors().isEmpty()) {
                // Có màu sắc -> hoàn trả stock cho màu đó và cập nhật tổng stock
                ProductVariant.ColorOption color = productVariant.getColors().stream()
                        .filter(c -> c.getId().equals(item.getColorId()))
                        .findFirst()
                        .orElse(null);

                if (color != null) {
                    color.setStock(color.getStock() + item.getQuantity());

                    // Cập nhật tổng stock = tổng stock của tất cả màu
                    int totalStock = productVariant.getColors().stream().mapToInt(ProductVariant.ColorOption::getStock)
                            .sum();
                    productVariant.setStock(totalStock);
                }
            } else {
                // Không có màu sắc -> hoàn trả stock tổng
                productVariant.setStock(productVariant.getStock() + item.getQuantity());
            }

            productVariantRepository.save(productVariant);
        }
        // 5. Cập nhật trạng thái đơn hàng
        orderRepository.save(order);

        // 6. Gửi thông báo cho chủ shop về việc hủy đơn
        try {
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Đơn hàng bị hủy",
                    String.format("Khách hàng %s đã hủy đơn hàng #%s", user.getFullName(), order.getId()));
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
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
    private boolean isPromotionValid(Promotion promotion, BigDecimal orderTotal, User user, Store store) {
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
        // 6. Kiểm tra mã chỉ dành cho người dùng mới
        if (promotion.getIsNewUserOnly() != null && promotion.getIsNewUserOnly()) {
            long orderCount = orderRepository.countByBuyerId(user.getId());
            if (orderCount > 0) {
                return false;
            }
        }

        // 7. Kiểm tra giới hạn sử dụng mỗi người
        if (promotion.getUsageLimitPerUser() != null) {
            int userUsageCount = promotionUsageRepository.countByPromotionAndUser(promotion, user);
            if (userUsageCount >= promotion.getUsageLimitPerUser()) {
                return false;
            }
        }

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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

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
    @Transactional
    public Order confirmOrder(String storeId, String orderId) throws Exception {
        Order order = getStoreOrderDetail(storeId, orderId);

        order.setStatus(Order.OrderStatus.CONFIRMED.name());
        Order savedOrder = orderRepository.save(order);

        // Gửi thông báo cho người mua về sự thay đổi trạng thái
        try {
            String statusMessage = getStatusMessage(Order.OrderStatus.CONFIRMED.name());
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Cập nhật trạng thái đơn hàng",
                    String.format("Đơn hàng #%s của bạn đã được cập nhật: %s",
                            order.getId(), statusMessage));
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }

        // Tạo Shipment
        try {
            shipmentService.createShipment(order.getId());
        } catch (Exception e) {
            System.err.println("Error creating shipment: " + e.getMessage());
        }

        return savedOrder;
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

    @Override
    public Order rejectOrder(String storeId, String orderId, String reason) throws Exception {
        Order order = getStoreOrderDetail(storeId, orderId);
        order.setStatus(Order.OrderStatus.CANCELLED.name());
        order.setRejectReason(reason);
        return orderRepository.save(order);
    }

    // Lấy giá của sản phẩm (có thể theo màu)
    private Long getProductPrice(ProductVariant productVariant, String colorId) {
        if (colorId != null && productVariant.getColors() != null && !productVariant.getColors().isEmpty()) {
            // Có chọn màu sắc cụ thể
            return productVariant.getColors().stream()
                    .filter(color -> color.getId().equals(colorId))
                    .findFirst()
                    .map(ProductVariant.ColorOption::getPrice)
                    .orElse(productVariant.getPrice());
        } else {
            // Không chọn màu sắc -> dùng giá chung
            return productVariant.getPrice();
        }
    }

    /**
     * Tính tổng giá trị của 1 store
     */
    private BigDecimal calculateStoreTotal(List<OrderDTO.SelectedCartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDTO.SelectedCartItem item : items) {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
            if (productVariant != null) {
                Long itemPrice = getProductPrice(productVariant, item.getColorId());
                BigDecimal itemTotal = BigDecimal.valueOf(itemPrice)
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    /**
     * Lấy thông báo theo trạng thái đơn hàng
     */
    private String getStatusMessage(String status) {
        return switch (status) {
            case "CONFIRMED" -> "Đơn hàng đã được xác nhận";
            case "SHIPPING" -> "Đơn hàng đang được vận chuyển";
            case "DELIVERED" -> "Đơn hàng đã được giao";
            case "CANCELLED" -> "Đơn hàng đã bị hủy";
            default -> "Trạng thái đơn hàng: " + status;
        };
    }

}