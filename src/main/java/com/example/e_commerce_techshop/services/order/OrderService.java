package com.example.e_commerce_techshop.services.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_techshop.dtos.buyer.OrderDTO;
import com.example.e_commerce_techshop.exceptions.DataNotFoundException;
import com.example.e_commerce_techshop.exceptions.InvalidPromotionException;
import com.example.e_commerce_techshop.models.Address;
import com.example.e_commerce_techshop.models.AdminRevenue;
import com.example.e_commerce_techshop.models.Notification;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.OrderItem;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.models.ProductVariant.ColorOption;
import com.example.e_commerce_techshop.models.Promotion;
import com.example.e_commerce_techshop.models.RefundRequest;
import com.example.e_commerce_techshop.models.Shipment;
import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.models.Static.OrderFinancials;
import com.example.e_commerce_techshop.repositories.AdminRevenueRepository;
import com.example.e_commerce_techshop.repositories.CartRepository;
import com.example.e_commerce_techshop.repositories.OrderItemRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.repositories.PromotionRepository;
import com.example.e_commerce_techshop.repositories.PromotionUsageRepository;
import com.example.e_commerce_techshop.repositories.RefundRequestRepository;
import com.example.e_commerce_techshop.repositories.ShipmentRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.responses.ShipmentResponse;
import com.example.e_commerce_techshop.responses.buyer.OrderResponse;
import com.example.e_commerce_techshop.services.cart.ICartService;
import com.example.e_commerce_techshop.services.notification.INotificationService;
import com.example.e_commerce_techshop.services.promotion.IPromotionService;
import com.example.e_commerce_techshop.services.refund.IRefundService;
import com.example.e_commerce_techshop.services.shipping.RegionalShippingService;
import com.example.e_commerce_techshop.services.wallet.IWalletService;

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
    private final IRefundService refundService;
    private final IWalletService walletService;
    private final AdminRevenueRepository adminRevenueRepository;
    private final RefundRequestRepository refundRequestRepository;
    private final ShipmentRepository shipmentRepository;
    private final RegionalShippingService regionalShippingService;

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
        BigDecimal totalPaymentAmount = BigDecimal.ZERO; // Tổng tiền cần thanh toán cho tất cả đơn hàng

        // 10. Tạo 1 đơn hàng cho mỗi store (theo thứ tự đã sắp xếp)
        for (Map.Entry<String, List<OrderDTO.SelectedCartItem>> storeEntry : sortedStores) {
            String storeId = storeEntry.getKey();
            List<OrderDTO.SelectedCartItem> storeItems = storeEntry.getValue();

            // 10.1. Lấy thông tin store
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new DataNotFoundException("Không tìm thấy cửa hàng với ID: " + storeId));

            // 10.2. Tính toán các khoản tiền cho store này
            OrderFinancials financials = calculateOrderFinancials(
                    storeItems, storeId, store, user, orderDTO,
                    platformOrderPromotion, platformShippingPromotion,
                    platformOrderPromotionUsed, applyShippingToStores
            );

            // Cập nhật số lần đã dùng platform order promotion
            if (financials.isPlatformOrderPromotionApplied()) {
                platformOrderPromotionUsed++;
            }

            BigDecimal storeTotal = financials.getStoreTotal();
            BigDecimal storeDiscountAmount = financials.getStoreDiscountAmount();
            BigDecimal platformDiscountAmount = financials.getPlatformDiscountAmount();
            BigDecimal finalShippingFee = financials.getFinalShippingFee();
            BigDecimal platformCommission = financials.getPlatformCommission();
            BigDecimal finalTotal = financials.getFinalTotal();
            List<Promotion> appliedPromotions = financials.getAppliedPromotions();

            // Cộng dồn vào tổng tiền thanh toán
            totalPaymentAmount = totalPaymentAmount.add(finalTotal);

            // Đảm bảo tất cả promotions được reload từ DB để có managed state
            List<Promotion> managedPromotions = new ArrayList<>();
            if (appliedPromotions != null && !appliedPromotions.isEmpty()) {
                for (Promotion promo : appliedPromotions) {
                    if (promo != null && promo.getId() != null) {
                        // Reload từ DB để đảm bảo managed state
                        promotionRepository.findById(promo.getId()).ifPresent(managedPromotions::add);
                    }
                }
            }

            // 10.4. Tạo Order cho store này
            Order order = Order.builder()
                    .buyer(user)
                    .store(store)
                    .promotions(managedPromotions.isEmpty() ? null : managedPromotions)
                    .productPrice(storeTotal) // Giá sản phẩm
                    .shippingFee(finalShippingFee) // Phí ship
                    .platformCommission(platformCommission) // Hoa hồng sàn 5%
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
                            .suggestedName(orderDTO.getAddress().getSuggestedName())
                            .build())
                    .paymentMethod(orderDTO.getPaymentMethod())
                    .paymentStatus(Order.PaymentStatus.UNPAID.name())
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
                                order.getId(), order.getTotalPrice().doubleValue()),
                        order.getId());

                // Thông báo cho chủ shop (store)
                notificationService.createStoreNotification(order.getStore().getId(),
                        "Có đơn hàng mới",
                        String.format("Bạn nhận được đơn hàng mới #%s từ khách %s.",
                                order.getId(), user.getFullName()),
                        order.getId());
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

            // Lấy tất cả promotion IDs từ các orders
            List<String> allPromotionIds = new ArrayList<>();
            for (Order order : orderPage.getContent()) {
                if (order.getPromotions() != null && !order.getPromotions().isEmpty()) {
                    order.getPromotions().stream()
                            .map(Promotion::getId)
                            .filter(id -> id != null)
                            .forEach(allPromotionIds::add);
                }
            }

            // Load promotions nếu có
            Map<String, Promotion> promotionsById = new HashMap<>();
            if (!allPromotionIds.isEmpty()) {
                List<Promotion> promotions = promotionRepository.findAllById(allPromotionIds);
                promotionsById = promotions.stream()
                        .collect(Collectors.toMap(Promotion::getId, p -> p));
            }

            // Assign orderItems và promotions to each order
            Map<String, Promotion> finalPromotionsById = promotionsById;
            orderPage.getContent().forEach(order -> {
                List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>());
                order.setOrderItems(items);
                
                // Set promotions
                if (order.getPromotions() != null && !order.getPromotions().isEmpty()) {
                    List<Promotion> orderPromotions = order.getPromotions().stream()
                            .map(p -> finalPromotionsById.get(p.getId()))
                            .filter(p -> p != null)
                            .toList();
                    order.setPromotions(orderPromotions);
                }
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
        
        // Load promotions (vì là @DBRef)
        if (order.getPromotions() != null && !order.getPromotions().isEmpty()) {
            List<String> promotionIds = order.getPromotions().stream()
                    .map(Promotion::getId)
                    .filter(id -> id != null)
                    .toList();
            if (!promotionIds.isEmpty()) {
                List<Promotion> promotions = promotionRepository.findAllById(promotionIds);
                order.setPromotions(promotions);
            }
        }

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

        // Hoàn tiền nếu đã thanh toán
        if (order.getPaymentStatus().equals(Order.PaymentStatus.PAID.name())) {
            try {
                refundService.createRefundRequest(order, order.getTotalPrice());
            } catch (Exception e) {
                System.err.println("Error creating refund request: " + e.getMessage());
            }
        }

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
                    String.format("Khách hàng %s đã hủy đơn hàng #%s", user.getFullName(), order.getId()),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Order completeOrder(User user, String orderId) throws Exception {
        String buyerId = user.getId();

        // Tìm order
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng"));

        // Kiểm tra trạng thái - chỉ cho phép complete đơn hàng DELIVERED
        if (!Order.OrderStatus.DELIVERED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể xác nhận hoàn tất đơn hàng đã giao");
        }

        // Cập nhật trạng thái
        order.setStatus(Order.OrderStatus.COMPLETED.name());
        
        Order savedOrder = orderRepository.save(order);

        // Cộng tiền vào ví shop
        try {
            // Tính doanh thu từ sản phẩm (sau khi trừ discount shop chịu)
            BigDecimal productRevenue = order.getProductPrice()
                    .subtract(order.getStoreDiscountAmount() != null ? order.getStoreDiscountAmount() : BigDecimal.ZERO);
            
            // Sàn lấy 5% hoa hồng từ doanh thu sản phẩm, tối đa 500,000đ
            BigDecimal platformCommission = productRevenue.multiply(BigDecimal.valueOf(0.05));
            BigDecimal maxCommission = BigDecimal.valueOf(500000);
            platformCommission = platformCommission.min(maxCommission);
            
            // Shop nhận 95% doanh thu sản phẩm + phí ship đầy đủ
            // Không trừ serviceFee vì serviceFee là phí khách hàng trả cho sàn (đã bao gồm trong totalPrice)
            BigDecimal storeRevenue = productRevenue.subtract(platformCommission)
                    .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);

            walletService.transferPendingToBalance(order.getStore().getId(), order.getId(), storeRevenue, String.format("Thanh toán đơn hàng #%s ", order.getId()));
            
            // Lưu hoa hồng sàn vào AdminRevenue để thống kê
            try {
                AdminRevenue platformCommissionRevenue = AdminRevenue.builder()
                        .order(order)
                        .amount(platformCommission)
                        .revenueType(AdminRevenue.RevenueType.PLATFORM_COMMISSION.name())
                        .description(String.format("Hoa hồng 5%% từ đơn hàng #%s", order.getId()))
                        .build();
                adminRevenueRepository.save(platformCommissionRevenue);
            } catch (Exception ex) {
                System.err.println("Error saving platform commission to admin revenue: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error adding payment to wallet: " + e.getMessage());
            notificationService.createAdminNotification(
                    "Lỗi cộng tiền vào ví shop",
                    String.format("Đơn hàng #%s của shop %s đã hoàn tất nhưng không thể cộng tiền vào ví. Vui lòng kiểm tra hệ thống.",
                            order.getId(), order.getStore().getName()),
                    Notification.NotificationType.SYSTEM.name(),
                    order.getId()
            );
            // Vẫn tiếp tục xử lý đơn hàng ngay cả khi có lỗi cộng tiền vào ví
        }

        // Gửi thông báo cho chủ shop
        try {
            notificationService.createStoreNotification(order.getStore().getId(),
                    "Đơn hàng hoàn tất",
                    String.format("Khách hàng %s đã xác nhận hoàn tất đơn hàng #%s", user.getFullName(), order.getId()),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }

        return savedOrder;
    }

    /**
     * Validate payment method
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        List<String> validMethods = List.of("COD", "VNPAY", "MOMO");
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
    public Page<OrderResponse> getStoreOrders(String storeId, String status, Pageable pageable) throws Exception {
        Page<Order> orders;
        if (status != null && !status.trim().isEmpty()) {
            orders = orderRepository.findByStoreIdAndStatus(storeId, status, pageable);
        } else {
            orders = orderRepository.findByStoreId(storeId, pageable);
        }

        if (!orders.getContent().isEmpty()) {
            List<String> orderIds = orders.getContent().stream()
                    .map(Order::getId)
                    .toList();

            List<OrderItem> allOrderItems = orderItemRepository.findByOrderIdIn(orderIds);

            Map<String, List<OrderItem>> itemsByOrderId = allOrderItems.stream()
                    .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

            // Lấy tất cả promotion IDs từ các orders
            List<String> allPromotionIds = new ArrayList<>();
            for (Order order : orders.getContent()) {
                if (order.getPromotions() != null && !order.getPromotions().isEmpty()) {
                    order.getPromotions().stream()
                            .map(Promotion::getId)
                            .filter(id -> id != null)
                            .forEach(allPromotionIds::add);
                }
            }

            // Load promotions nếu có
            Map<String, Promotion> promotionsById = new HashMap<>();
            if (!allPromotionIds.isEmpty()) {
                List<Promotion> promotions = promotionRepository.findAllById(allPromotionIds);
                promotionsById = promotions.stream()
                        .collect(Collectors.toMap(Promotion::getId, p -> p));
            }

            // Assign orderItems và promotions to each order
            Map<String, Promotion> finalPromotionsById = promotionsById;
            orders.getContent().forEach(order -> {
                List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>());
                order.setOrderItems(items);
                
                // Set promotions
                if (order.getPromotions() != null && !order.getPromotions().isEmpty()) {
                    List<Promotion> orderPromotions = order.getPromotions().stream()
                            .map(p -> finalPromotionsById.get(p.getId()))
                            .filter(p -> p != null)
                            .toList();
                    order.setPromotions(orderPromotions);
                }
            });
        }

        return orders.map(OrderResponse::fromOrder);
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
        
        // Load promotions (vì là @DBRef)
        if (order.getPromotions() != null && !order.getPromotions().isEmpty()) {
            List<String> promotionIds = order.getPromotions().stream()
                    .map(Promotion::getId)
                    .filter(id -> id != null)
                    .toList();
            if (!promotionIds.isEmpty()) {
                List<Promotion> promotions = promotionRepository.findAllById(promotionIds);
                order.setPromotions(promotions);
            }
        }

        return order;
    }

    @Override
    @Transactional
    public Order confirmOrder(String storeId, String orderId) throws Exception {
        Order order = getStoreOrderDetail(storeId, orderId);

        // Kiểm tra nếu phương thức thanh toán online thì phải đã thanh toán
        if ("VNPAY".equalsIgnoreCase(order.getPaymentMethod()) || 
            "MOMO".equalsIgnoreCase(order.getPaymentMethod())) {
            if (!Order.PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
                throw new IllegalArgumentException(
                    "Không thể xác nhận đơn hàng. Khách hàng chưa thanh toán thành công. " +
                    "Vui lòng chờ khách hàng hoàn tất thanh toán trước khi xác nhận đơn hàng."
                );
            }
        }

        order.setStatus(Order.OrderStatus.CONFIRMED.name());
        Order savedOrder = orderRepository.save(order);

        // Gửi thông báo cho người mua về sự thay đổi trạng thái
        try {
            String statusMessage = getStatusMessage(Order.OrderStatus.CONFIRMED.name());
            notificationService.createUserNotification(order.getBuyer().getId(),
                    "Cập nhật trạng thái đơn hàng",
                    String.format("Đơn hàng #%s của bạn đã được cập nhật: %s",
                            order.getId(), statusMessage),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }

        return savedOrder;
    }

    @Override
    @Transactional
    public Order rejectOrder(String storeId, String orderId, String reason) throws Exception {
        Order order = getStoreOrderDetail(storeId, orderId);
        
        // Chỉ cho phép từ chối đơn hàng PENDING hoặc CONFIRMED
        if (!Order.OrderStatus.PENDING.name().equals(order.getStatus()) && 
            !Order.OrderStatus.CONFIRMED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối đơn hàng ở trạng thái PENDING hoặc CONFIRMED");
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED.name());
        order.setRejectReason(reason);
        
        // Hoàn trả stock
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : orderItems) {
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
                    int totalStock = productVariant.getColors().stream()
                            .mapToInt(ProductVariant.ColorOption::getStock)
                            .sum();
                    productVariant.setStock(totalStock);
                }
            } else {
                // Không có màu sắc -> hoàn trả stock tổng
                productVariant.setStock(productVariant.getStock() + item.getQuantity());
            }

            productVariantRepository.save(productVariant);
        }
        
        // Hoàn tiền nếu đã thanh toán (MoMo/VNPay)
        if (Order.PaymentStatus.PAID.name().equals(order.getPaymentStatus())) {
            try {
                // Tạo yêu cầu hoàn tiền
                refundService.createRefundRequest(order, order.getTotalPrice());
                
                // Trừ pendingAmount vì đơn hàng thanh toán online đã được cộng pending trước đó
                try {
                    BigDecimal productRevenue = order.getProductPrice()
                            .subtract(order.getStoreDiscountAmount() != null ? order.getStoreDiscountAmount() : BigDecimal.ZERO);
                    BigDecimal platformCommission = productRevenue.multiply(BigDecimal.valueOf(0.05));
                    BigDecimal maxCommission = BigDecimal.valueOf(500000);
                    platformCommission = platformCommission.min(maxCommission);
                    BigDecimal storeRevenue = productRevenue.subtract(platformCommission)
                            .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
                    
                    walletService.deductFromPendingAmount(
                            order.getStore().getId(),
                            order.getId(),
                            storeRevenue,
                            String.format("Shop từ chối đơn hàng #%s - trừ tiền chờ", order.getId())
                    );  
                } catch (Exception ex) {
                    System.err.println("Error deducting from pending amount: " + ex.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Error creating refund request: " + e.getMessage());
                // Vẫn cho phép từ chối đơn nhưng ghi log lỗi
                // Admin sẽ xử lý hoàn tiền thủ công
            }
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Thông báo cho khách hàng về việc từ chối đơn
        try {
            notificationService.createUserNotification(
                    order.getBuyer().getId(),
                    "Đơn hàng bị từ chối",
                    String.format("Cửa hàng %s đã từ chối đơn hàng #%s. Lý do: %s", 
                            order.getStore().getName(), order.getId(), reason),
                    order.getId());
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
        }
        
        return savedOrder;
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
     * Tính toán tất cả các khoản tiền cho một đơn hàng
     * 
     * @param storeItems Danh sách sản phẩm của store
     * @param storeId ID của store
     * @param store Đối tượng Store
     * @param user Người mua
     * @param orderDTO Dữ liệu đơn hàng
     * @param platformOrderPromotion Mã giảm giá đơn hàng của sàn
     * @param platformShippingPromotion Mã giảm phí ship của sàn
     * @param platformOrderPromotionUsed Số lần đã dùng platform order promotion
     * @param applyShippingToStores Danh sách store được áp dụng shipping voucher
     * @return OrderFinancials chứa tất cả thông tin tài chính
     */
    private OrderFinancials calculateOrderFinancials(
            List<OrderDTO.SelectedCartItem> storeItems,
            String storeId,
            Store store,
            User user,
            OrderDTO orderDTO,
            Promotion platformOrderPromotion,
            Promotion platformShippingPromotion,
            int platformOrderPromotionUsed,
            List<String> applyShippingToStores) {

        // 1. Tính tổng tiền sản phẩm của store
        BigDecimal storeTotal = BigDecimal.ZERO;
        for (OrderDTO.SelectedCartItem item : storeItems) {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId()).get();
            Long itemPrice = getProductPrice(productVariant, item.getColorId());
            BigDecimal itemTotal = BigDecimal.valueOf(itemPrice)
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            storeTotal = storeTotal.add(itemTotal);
        }

        // 2. Khởi tạo các biến tính toán
        List<Promotion> appliedPromotions = new ArrayList<>();
        BigDecimal storeDiscountAmount = BigDecimal.ZERO; // Tiền shop chịu
        BigDecimal platformDiscountAmount = BigDecimal.ZERO; // Tiền sàn chịu
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        boolean isPlatformOrderPromotionApplied = false;

        BigDecimal currentTotal = storeTotal; // Track giá trị sau mỗi lần discount

        // 3. Áp dụng Store ORDER Promotion (TRƯỚC)
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

        // 4. Áp dụng Platform ORDER Promotion (SAU store voucher)
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
                    isPlatformOrderPromotionApplied = true;
                }
            }
        }

        // 5. Tính phí ship động theo vùng miền
        BigDecimal shippingFee = regionalShippingService.calculateShippingFee(
                store.getAddress(),
                Address.builder()
                        .province(orderDTO.getAddress().getProvince())
                        .ward(orderDTO.getAddress().getWard())
                        .homeAddress(orderDTO.getAddress().getHomeAddress())
                        .build()
        );

        // 6. Áp dụng Platform SHIPPING Promotion
        if (platformShippingPromotion != null && applyShippingToStores.contains(storeId)) {
            // Check minOrderValue với storeTotal gốc (không phải currentTotal)
            if (platformShippingPromotion.getMinOrderValue() == null ||
                    storeTotal.compareTo(BigDecimal.valueOf(platformShippingPromotion.getMinOrderValue())) >= 0) {

                BigDecimal platformShippingDiscount = calculateDiscount(shippingFee, platformShippingPromotion);
                shippingDiscount = shippingDiscount.add(platformShippingDiscount);
                appliedPromotions.add(platformShippingPromotion);
            }
        }

        // 7. Tính phí ship cuối cùng sau khi áp dụng giảm giá
        BigDecimal finalShippingFee = shippingFee.subtract(shippingDiscount).max(BigDecimal.ZERO);

        // 8. Tính hoa hồng sàn (5% doanh thu sản phẩm của shop, tối đa 500k)
        BigDecimal productRevenue = storeTotal.subtract(storeDiscountAmount);
        BigDecimal platformCommission = productRevenue.multiply(BigDecimal.valueOf(0.05));
        // Giới hạn hoa hồng tối đa 500,000đ mỗi đơn
        BigDecimal maxCommission = BigDecimal.valueOf(500000);
        platformCommission = platformCommission.min(maxCommission);

        // 9. Tính tổng tiền cuối cùng khách hàng phải thanh toán
        BigDecimal totalDiscount = storeDiscountAmount.add(platformDiscountAmount);
        BigDecimal finalTotal = storeTotal.subtract(totalDiscount).add(finalShippingFee).max(BigDecimal.ZERO);

        // 10. Trả về kết quả
        return new OrderFinancials(
                storeTotal,
                storeDiscountAmount,
                platformDiscountAmount,
                totalDiscount,
                finalShippingFee,
                platformCommission,
                finalTotal,
                appliedPromotions,
                isPlatformOrderPromotionApplied
        );
    }



    /**
     * Lấy thông báo theo trạng thái đơn hàng
     */
    private String getStatusMessage(String status) {
        return switch (status) {
            case "PENDING" -> "Đơn hàng đang chờ xử lý";
            case "CONFIRMED" -> "Đơn hàng đã được xác nhận";
            case "SHIPPING" -> "Đơn hàng đang được vận chuyển";
            case "DELIVERED" -> "Đơn hàng đã được giao";
            case "COMPLETED" -> "Đơn hàng đã hoàn tất";
            case "CANCELLED" -> "Đơn hàng đã bị hủy";
            default -> "Trạng thái đơn hàng: " + status;
        };
    }

    @Override
    public Map<String, Long> countOrdersByStatus(String storeId) throws Exception {
        Map<String, Long> statusCounts = new HashMap<>();

        long pending = orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.PENDING.name());
        long confirmed = orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.CONFIRMED.name());
        long shipping = orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.SHIPPING.name());
        long delivered = orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.DELIVERED.name());
        long cancelled = orderRepository.countByStoreIdAndStatus(storeId, Order.OrderStatus.CANCELLED.name());

        long total = pending + confirmed + shipping + delivered + cancelled;

        statusCounts.put("total", total);
        statusCounts.put("pending", pending);
        statusCounts.put("confirmed", confirmed);
        statusCounts.put("shipping", shipping);
        statusCounts.put("delivered", delivered);
        statusCounts.put("cancelled", cancelled);

        return statusCounts;
    }

    @Override
    @Transactional
    public void updatePaymentStatus(String orderId, String paymentStatus, Long momoTransId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Đơn hàng không tồn tại: " + orderId));

        // Cập nhật trạng thái thanh toán
        order.setPaymentStatus(paymentStatus);

        // Lưu mã giao dịch MoMo nếu có
        if (momoTransId != null && momoTransId > 0) {
            order.setMomoTransId(String.valueOf(momoTransId));
        }

        // Nếu thanh toán thành công và đơn hàng đang PENDING
        if ("PAID".equals(paymentStatus) && Order.OrderStatus.PENDING.name().equals(order.getStatus())) {
            order.setPaymentStatus(Order.PaymentStatus.PAID.name());
            
            // Cộng tiền vào pendingAmount của ví shop (thanh toán online đã nhận tiền)
            try {
                // Tính doanh thu shop sẽ nhận
                BigDecimal productRevenue = order.getProductPrice()
                        .subtract(order.getStoreDiscountAmount() != null ? order.getStoreDiscountAmount() : BigDecimal.ZERO);
                
                // Sàn lấy 5% hoa hồng, tối đa 500,000đ
                BigDecimal platformCommission = productRevenue.multiply(BigDecimal.valueOf(0.05));
                BigDecimal maxCommission = BigDecimal.valueOf(500000);
                platformCommission = platformCommission.min(maxCommission);
                
                // Shop nhận 95% doanh thu + phí ship
                BigDecimal storeRevenue = productRevenue.subtract(platformCommission)
                        .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO);
                
                walletService.addToPendingAmount(
                        order.getStore().getId(),
                        order.getId(),
                        storeRevenue,
                        String.format("Tiền chờ từ đơn hàng #%s (thanh toán online)", order.getId())
                );
            } catch (Exception e) {
                System.err.println("Error adding to pending amount: " + e.getMessage());
            }
            
            // Gửi thông báo cho cửa hàng
            try {
                notificationService.createStoreNotification(
                        order.getStore().getId(),
                        "Đơn hàng mới",
                        "Bạn có đơn hàng mới #" + order.getId() + " đã thanh toán thành công",
                        order.getId());
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        }

        // Nếu thanh toán thất bại, có thể tự động hủy đơn
        if ("FAILED".equals(paymentStatus) && Order.OrderStatus.PENDING.name().equals(order.getStatus())) {
            order.setStatus(Order.OrderStatus.CANCELLED.name());
            try {
                notificationService.createUserNotification(
                        order.getBuyer().getId(),
                        "Thanh toán thất bại",
                        "Thanh toán cho đơn hàng #" + order.getId() + " đã thất bại. Đơn hàng bị hủy.",
                        order.getId());
            } catch (Exception e) {
                System.out.println("Failed to send notification: " + e.getMessage());
            }
            System.out.println("Payment failed for order: " + orderId + ". Order status remains PENDING.");
        }

        orderRepository.save(order);
    }

    @Override
    public Order getOrderById(String orderId) throws Exception {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Đơn hàng không tồn tại: " + orderId));
    }

    @Override
    public OrderResponse.RefundInfo getOrderRefundInfo(User user, String orderId) throws Exception {
        // Kiểm tra order tồn tại và thuộc về user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem thông tin đơn hàng này");
        }

        // Tìm RefundRequest cho order này
        RefundRequest refundRequest = refundRequestRepository.findByOrderId(orderId)
            .orElse(null);

        if (refundRequest == null) {
            return null; // Chưa có hoàn tiền
        }

        // Map sang RefundInfo
        return OrderResponse.RefundInfo.builder()
                .refundRequestId(refundRequest.getId())
                .refundAmount(refundRequest.getRefundAmount())
                .refundMethod(refundRequest.getPaymentMethod())
                .status(refundRequest.getStatus())
                .refundTransactionId(refundRequest.getRefundTransactionId())
                .refundCompletedAt(order.getRefundCompletedAt())
                .bankName(refundRequest.getBankName())
                .bankAccountNumber(refundRequest.getBankAccountNumber())
                .bankAccountName(refundRequest.getBankAccountName())
                .adminNote(refundRequest.getAdminNote())
                .rejectionReason(refundRequest.getRejectionReason())
                .build();
    }

    @Override
    public ShipmentResponse getReturnShipmentInfo(User user, String orderId) throws Exception {
        // Kiểm tra order tồn tại và thuộc về user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getBuyer().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không có quyền xem thông tin đơn hàng này");
        }

        // Tìm shipment trả hàng của order này (isReturnShipment = true)
        Shipment shipment = shipmentRepository.findByOrderIdAndIsReturnShipment(orderId, true).orElse(null);

        if (shipment == null) {
            return null; // Chưa có shipment trả hàng
        }
        return ShipmentResponse.fromShipment(shipment);
    }

}