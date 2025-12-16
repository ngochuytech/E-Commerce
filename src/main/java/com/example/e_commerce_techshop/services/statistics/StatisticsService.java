package com.example.e_commerce_techshop.services.statistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.e_commerce_techshop.models.AdminRevenue;
import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.OrderItem;
import com.example.e_commerce_techshop.models.ProductVariant;
import com.example.e_commerce_techshop.repositories.AdminRevenueRepository;
import com.example.e_commerce_techshop.repositories.OrderItemRepository;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ProductRepository;
import com.example.e_commerce_techshop.repositories.ProductVariantRepository;
import com.example.e_commerce_techshop.repositories.PromotionRepository;
import com.example.e_commerce_techshop.repositories.StoreRepository;
import com.example.e_commerce_techshop.repositories.user.UserRepository;
import com.example.e_commerce_techshop.responses.admin.AdminRevenueResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatisticsService implements IStatisticsService {
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final AdminRevenueRepository adminRevenueRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Map<String, Object> getAdminOverviewStatistics() throws Exception {
        Map<String, Object> statistics = new HashMap<>();
        try {
            long totalPendingStores = storeRepository.countByStatus("PENDING");
            statistics.put("totalPendingStores", totalPendingStores);

            long totalPendingProducts = productRepository.countByStatus("PENDING");
            statistics.put("totalPendingProducts", totalPendingProducts);

            long totalPendingVariants = productVariantRepository.countByStatus("PENDING");
            statistics.put("totalPendingVariants", totalPendingVariants);

            long totalUsers = userRepository.count();
            statistics.put("totalUsers", totalUsers);

            long totalPromotions = promotionRepository.count();
            statistics.put("totalPromotions", totalPromotions);

        } catch (Exception e) {
            System.out.println("Lỗi lấy thống kê " + e.getMessage());
            statistics.put("totalPendingStores", 0);
            statistics.put("totalPendingProducts", 0);
            statistics.put("totalPendingVariants", 0);
            statistics.put("totalUsers", 0);
            statistics.put("totalPromotions", 0);
        }
        return statistics;
    }

    @Override
    public Map<String, Object> getAdminRevenueStatistics() {
        // Tổng hoa hồng nền tảng
        List<AdminRevenue> platformCommissions = adminRevenueRepository.findByRevenueType(AdminRevenue.RevenueType.PLATFORM_COMMISSION.name());
        BigDecimal totalPlatformCommission = platformCommissions.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền lỗ từ platform discount
        List<AdminRevenue> platformDiscountLoss = adminRevenueRepository.findByRevenueType(AdminRevenue.RevenueType.PLATFORM_DISCOUNT_LOSS.name());
        BigDecimal totalPlatformDiscountLoss = platformDiscountLoss.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng phí vận chuyển
        List<AdminRevenue> shippingFees = adminRevenueRepository.findByRevenueType(AdminRevenue.RevenueType.SHIPPING_FEE.name());
        BigDecimal totalShippingFee = shippingFees.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPlatformCommission", totalPlatformCommission);
        stats.put("totalPlatformDiscountLoss", totalPlatformDiscountLoss);
        stats.put("totalShippingFee", totalShippingFee);
        stats.put("netRevenue", totalPlatformCommission.add(totalShippingFee).subtract(totalPlatformDiscountLoss));
        stats.put("platformCommissionCount", platformCommissions.size());
        stats.put("platformDiscountLossCount", platformDiscountLoss.size());
        stats.put("shippingFeeCount", shippingFees.size());

        return stats;
    }

    @Override
    public Map<String, Object> getAdminPlatformCommissions(Pageable pageable) {
        List<AdminRevenue> allServiceFees = adminRevenueRepository.findByRevenueType(AdminRevenue.RevenueType.PLATFORM_COMMISSION.name(), pageable)
                .getContent();

        BigDecimal total = allServiceFees.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = allServiceFees.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("page", pageable.getPageNumber());
        response.put("size", pageable.getPageSize());
        response.put("totalAmount", total);

        return response;
    }

    @Override
    public Map<String, Object> getAdminPlatformDiscountLosses(Pageable pageable) {
        List<AdminRevenue> allLosses = adminRevenueRepository.findByRevenueType(AdminRevenue.RevenueType.PLATFORM_DISCOUNT_LOSS.name(), pageable)
                .getContent();
        BigDecimal total = allLosses.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = allLosses.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("total", allLosses.size());
        response.put("page", pageable.getPageNumber());
        response.put("size", pageable.getPageSize());
        response.put("totalAmount", total);

        return response;
    }

    @Override
    public Map<String, Object> getAdminShippingFees(Pageable pageable) {
        List<AdminRevenue> allShippingFees = adminRevenueRepository.findByRevenueType(AdminRevenue.RevenueType.SHIPPING_FEE.name(), pageable)
                .getContent();
        BigDecimal total = allShippingFees.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = allShippingFees.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("total", allShippingFees.size());
        response.put("page", pageable.getPageNumber());
        response.put("size", pageable.getPageSize());
        response.put("totalAmount", total);

        return response;
    }

    @Override
    public Map<String, Object> getAdminRevenueByDateRange(String startDate, String endDate, Pageable pageable) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        List<AdminRevenue> allRevenues = adminRevenueRepository.findByCreatedAtBetween(start, end, pageable)
                .getContent();

        BigDecimal total = allRevenues.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = allRevenues.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("page", pageable.getPageNumber());
        response.put("size", pageable.getPageSize());
        response.put("total", allRevenues.size());
        response.put("totalAmount", total);

        return response;
    }

    @Override
    public Map<String, Object> getRevenueChartData(String period) {
        Map<String, Object> chartData = new HashMap<>();

        // Lấy riêng biệt theo loại doanh thu
        List<AdminRevenue> serviceFeeRevenues = adminRevenueRepository.findByRevenueType("SERVICE_FEE");
        List<AdminRevenue> discountLossRevenues = adminRevenueRepository.findByRevenueType("PLATFORM_DISCOUNT_LOSS");

        if (serviceFeeRevenues.isEmpty() && discountLossRevenues.isEmpty()) {
            chartData.put("labels", new ArrayList<>());
            chartData.put("serviceFees", new ArrayList<>());
            chartData.put("discountLosses", new ArrayList<>());
            chartData.put("netRevenue", new ArrayList<>());
            return chartData;
        }

        Map<String, BigDecimal> serviceFeeData = new LinkedHashMap<>();
        Map<String, BigDecimal> discountLossData = new LinkedHashMap<>();

        if ("WEEK".equalsIgnoreCase(period)) {
            getWeeklyDataSeparate(serviceFeeRevenues, discountLossRevenues, serviceFeeData, discountLossData);
        } else if ("MONTH".equalsIgnoreCase(period)) {
            getMonthlyDataSeparate(serviceFeeRevenues, discountLossRevenues, serviceFeeData, discountLossData);
        } else if ("YEAR".equalsIgnoreCase(period)) {
            getYearlyDataSeparate(serviceFeeRevenues, discountLossRevenues, serviceFeeData, discountLossData);
        }

        List<String> labels = new ArrayList<>(serviceFeeData.keySet());
        List<BigDecimal> serviceFees = new ArrayList<>(serviceFeeData.values());
        List<BigDecimal> discountLosses = new ArrayList<>(discountLossData.values());
        List<BigDecimal> netRevenues = new ArrayList<>();

        for (int i = 0; i < serviceFees.size(); i++) {
            netRevenues.add(serviceFees.get(i).subtract(discountLosses.get(i)));
        }

        chartData.put("labels", labels);
        chartData.put("serviceFees", serviceFees);
        chartData.put("serviceFeeLabel", "Phí dịch vụ");
        chartData.put("discountLosses", discountLosses);
        chartData.put("discountLossLabel", "Mất mát từ giảm giá");
        chartData.put("netRevenue", netRevenues);
        chartData.put("netRevenueLabel", "Doanh thu ròng");
        chartData.put("period", period.toUpperCase());

        return chartData;
    }

    private void getWeeklyDataSeparate(List<AdminRevenue> serviceFeeRevenues, List<AdminRevenue> discountLossRevenues,
            Map<String, BigDecimal> serviceFeeData, Map<String, BigDecimal> discountLossData) {
        WeekFields weekFields = WeekFields.ISO;

        // Xử lý service fees
        serviceFeeRevenues.stream()
                .filter(r -> r.getCreatedAt() != null && r.getAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> {
                            LocalDateTime createdAt = r.getCreatedAt();
                            int weekOfYear = createdAt.get(weekFields.weekOfYear());
                            int year = createdAt.getYear();
                            return String.format("Tuần %d/%d", weekOfYear, year);
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(AdminRevenue::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .forEach(serviceFeeData::put);

        // Xử lý discount losses
        discountLossRevenues.stream()
                .filter(r -> r.getCreatedAt() != null && r.getAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> {
                            LocalDateTime createdAt = r.getCreatedAt();
                            int weekOfYear = createdAt.get(weekFields.weekOfYear());
                            int year = createdAt.getYear();
                            return String.format("Tuần %d/%d", weekOfYear, year);
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(AdminRevenue::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .forEach(discountLossData::put);

        // Đảm bảo cả hai map có cùng keys
        serviceFeeData.keySet().forEach(week -> discountLossData.putIfAbsent(week, BigDecimal.ZERO));
        discountLossData.keySet().forEach(week -> serviceFeeData.putIfAbsent(week, BigDecimal.ZERO));
    }

    private void getMonthlyDataSeparate(List<AdminRevenue> serviceFeeRevenues, List<AdminRevenue> discountLossRevenues,
            Map<String, BigDecimal> serviceFeeData, Map<String, BigDecimal> discountLossData) {

        // Xử lý service fees
        serviceFeeRevenues.stream()
                .filter(r -> r.getCreatedAt() != null && r.getAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> {
                            YearMonth yearMonth = YearMonth.from(r.getCreatedAt());
                            return yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy",
                                    java.util.Locale.forLanguageTag("vi_VN")));
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(AdminRevenue::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .forEach(serviceFeeData::put);

        // Xử lý discount losses
        discountLossRevenues.stream()
                .filter(r -> r.getCreatedAt() != null && r.getAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> {
                            YearMonth yearMonth = YearMonth.from(r.getCreatedAt());
                            return yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy",
                                    java.util.Locale.forLanguageTag("vi_VN")));
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(AdminRevenue::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .forEach(discountLossData::put);

        // Đảm bảo cả hai map có cùng keys
        serviceFeeData.keySet().forEach(month -> discountLossData.putIfAbsent(month, BigDecimal.ZERO));
        discountLossData.keySet().forEach(month -> serviceFeeData.putIfAbsent(month, BigDecimal.ZERO));
    }

    private void getYearlyDataSeparate(List<AdminRevenue> serviceFeeRevenues, List<AdminRevenue> discountLossRevenues,
            Map<String, BigDecimal> serviceFeeData, Map<String, BigDecimal> discountLossData) {

        // Xử lý service fees
        serviceFeeRevenues.stream()
                .filter(r -> r.getCreatedAt() != null && r.getAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> String.format("Năm %d", r.getCreatedAt().getYear()),
                        LinkedHashMap::new,
                        Collectors.mapping(AdminRevenue::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .forEach(serviceFeeData::put);

        // Xử lý discount losses
        discountLossRevenues.stream()
                .filter(r -> r.getCreatedAt() != null && r.getAmount() != null)
                .collect(Collectors.groupingBy(
                        r -> String.format("Năm %d", r.getCreatedAt().getYear()),
                        LinkedHashMap::new,
                        Collectors.mapping(AdminRevenue::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .forEach(discountLossData::put);

        // Đảm bảo cả hai map có cùng keys
        serviceFeeData.keySet().forEach(year -> discountLossData.putIfAbsent(year, BigDecimal.ZERO));
        discountLossData.keySet().forEach(year -> serviceFeeData.putIfAbsent(year, BigDecimal.ZERO));
    }

    @Override
    public Map<String, Object> getStoreOverviewStatistics(String storeId) {
        Map<String, Object> overview = new HashMap<>();

        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            List<Order> todayOrders = orderRepository.findByStoreIdAndStatusAndDateRange(storeId, "DELIVERED",
                    startOfDay, endOfDay);

            BigDecimal todayRevenue = todayOrders.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long newOrdersToday = todayOrders.size();
            long variantAmount = productVariantRepository.countByStoreIdAndStatus(storeId, "APPROVED");

            overview.put("todayRevenue", todayRevenue);
            overview.put("newOrdersToday", newOrdersToday);
            overview.put("variantAmount", variantAmount);

        } catch (Exception e) {
            System.out.println("Lỗi lấy thống kê store overview: " + e.getMessage());
            overview.put("todayRevenue", BigDecimal.ZERO);
            overview.put("newOrdersToday", 0);
        }

        return overview;
    }

    @Override
    public Map<String, Object> getStoreRevenueChartData(String storeId, String period) throws Exception {
        Map<String, Object> chartData = new HashMap<>();

        List<Order> allOrders = orderRepository.findByStoreIdAndStatus(storeId, "DELIVERED");

        if (allOrders.isEmpty()) {
            chartData.put("labels", new ArrayList<>());
            chartData.put("revenues", new ArrayList<>());
            chartData.put("revenueLabel", "Doanh thu");
            chartData.put("period", period.toUpperCase());
            return chartData;
        }

        Map<String, BigDecimal> revenueData = new LinkedHashMap<>();

        if ("WEEK".equalsIgnoreCase(period)) {
            getStoreWeeklyRevenueData(allOrders, revenueData);
        } else if ("MONTH".equalsIgnoreCase(period)) {
            getStoreMonthlyRevenueData(allOrders, revenueData);
        } else if ("YEAR".equalsIgnoreCase(period)) {
            getStoreYearlyRevenueData(allOrders, revenueData);
        }

        List<String> labels = new ArrayList<>(revenueData.keySet());
        List<BigDecimal> revenues = new ArrayList<>(revenueData.values());

        chartData.put("labels", labels);
        chartData.put("revenues", revenues);
        chartData.put("revenueLabel", "Doanh thu");
        chartData.put("period", period.toUpperCase());

        return chartData;
    }

    private void getStoreWeeklyRevenueData(List<Order> orders, Map<String, BigDecimal> revenueData) {
        WeekFields weekFields = WeekFields.ISO;

        orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getTotalPrice() != null)
                .collect(Collectors.groupingBy(
                        o -> {
                            LocalDateTime createdAt = o.getCreatedAt();
                            int weekOfYear = createdAt.get(weekFields.weekOfYear());
                            int year = createdAt.getYear();
                            return String.format("Tuần %d/%d", weekOfYear, year);
                        },
                        LinkedHashMap::new,
                        Collectors.toList()))
                .forEach((week, items) -> {
                    BigDecimal totalRevenue = items.stream()
                            .map(Order::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    revenueData.put(week, totalRevenue);
                });
    }

    private void getStoreMonthlyRevenueData(List<Order> orders, Map<String, BigDecimal> revenueData) {
        orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getTotalPrice() != null)
                .collect(Collectors.groupingBy(
                        o -> {
                            YearMonth yearMonth = YearMonth.from(o.getCreatedAt());
                            return yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy",
                                    java.util.Locale.forLanguageTag("vi_VN")));
                        },
                        LinkedHashMap::new,
                        Collectors.toList()))
                .forEach((month, items) -> {
                    BigDecimal totalRevenue = items.stream()
                            .map(Order::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    revenueData.put(month, totalRevenue);
                });
    }

    private void getStoreYearlyRevenueData(List<Order> orders, Map<String, BigDecimal> revenueData) {
        orders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getTotalPrice() != null)
                .collect(Collectors.groupingBy(
                        o -> String.format("Năm %d", o.getCreatedAt().getYear()),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .forEach((year, items) -> {
                    BigDecimal totalRevenue = items.stream()
                            .map(Order::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    revenueData.put(year, totalRevenue);
                });
    }

    @Override
    public Map<String, Object> getStoreOrdersChartData(String storeId, String period) throws Exception {
        Map<String, Object> chartData = new HashMap<>();

        List<Order> allOrders = orderRepository.findByStoreIdAndStatus(storeId, "DELIVERED");

        if (allOrders.isEmpty()) {
            chartData.put("labels", new ArrayList<>());
            chartData.put("orderCounts", new ArrayList<>());
            chartData.put("orderCountLabel", "Số đơn hàng");
            chartData.put("period", period.toUpperCase());
            return chartData;
        }

        Map<String, Long> orderData = new LinkedHashMap<>();

        if ("WEEK".equalsIgnoreCase(period)) {
            getStoreWeeklyOrderCount(allOrders, orderData);
        } else if ("MONTH".equalsIgnoreCase(period)) {
            getStoreMonthlyOrderCount(allOrders, orderData);
        } else if ("YEAR".equalsIgnoreCase(period)) {
            getStoreYearlyOrderCount(allOrders, orderData);
        }

        List<String> labels = new ArrayList<>(orderData.keySet());
        List<Long> orderCounts = new ArrayList<>(orderData.values());

        chartData.put("labels", labels);
        chartData.put("orderCounts", orderCounts);
        chartData.put("orderCountLabel", "Số đơn hàng");
        chartData.put("period", period.toUpperCase());

        return chartData;
    }

    private void getStoreWeeklyOrderCount(List<Order> orders, Map<String, Long> orderData) {
        WeekFields weekFields = WeekFields.ISO;

        orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        o -> {
                            LocalDateTime createdAt = o.getCreatedAt();
                            int weekOfYear = createdAt.get(weekFields.weekOfYear());
                            int year = createdAt.getYear();
                            return String.format("Tuần %d/%d", weekOfYear, year);
                        },
                        LinkedHashMap::new,
                        Collectors.counting()))
                .forEach(orderData::put);
    }

    private void getStoreMonthlyOrderCount(List<Order> orders, Map<String, Long> orderData) {
        orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        o -> {
                            YearMonth yearMonth = YearMonth.from(o.getCreatedAt());
                            return yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy",
                                    java.util.Locale.forLanguageTag("vi_VN")));
                        },
                        LinkedHashMap::new,
                        Collectors.counting()))
                .forEach(orderData::put);
    }

    private void getStoreYearlyOrderCount(List<Order> orders, Map<String, Long> orderData) {
        orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        o -> String.format("Năm %d", o.getCreatedAt().getYear()),
                        LinkedHashMap::new,
                        Collectors.counting()))
                .forEach(orderData::put);
    }

    @Override
    public Map<String, Object> getOrderCountByStatus(String storeId) throws Exception {
        Map<String, Object> orderStats = new HashMap<>();
        long totalOrders = orderRepository.countByStoreId(storeId);
        long deliveredOrders = orderRepository.countByStoreIdAndStatus(storeId, "DELIVERED");
        long pendingOrders = orderRepository.countByStoreIdAndStatus(storeId, "PENDING");
        long cancelledOrders = orderRepository.countByStoreIdAndStatus(storeId, "CANCELLED");
        long shippingOrders = orderRepository.countByStoreIdAndStatus(storeId, "SHIPPING");
        long confirmedOrders = orderRepository.countByStoreIdAndStatus(storeId, "CONFIRMED");
        orderStats.put("totalOrders", totalOrders);
        orderStats.put("deliveredOrders", deliveredOrders);
        orderStats.put("pendingOrders", pendingOrders);
        orderStats.put("cancelledOrders", cancelledOrders);
        orderStats.put("shippingOrders", shippingOrders);
        orderStats.put("confirmedOrders", confirmedOrders);
        return orderStats;
    }

    @Override
    public Map<String, Object> getVariantCountByStockStatus(String storeId) throws Exception {
        Map<String, Object> variantStats = new HashMap<>();

        long totalProducts = productVariantRepository.countByStoreIdAndStatus(storeId, "APPROVED");

        // Sản phẩm sắp hết hàng (0 < stock <= 10)
        long lowStockProducts = productVariantRepository.countByStoreIdAndStatusAndLowStock(storeId, "APPROVED");

        // Sản phẩm hết hàng (stock = 0)
        long outOfStockProducts = productVariantRepository.countByStoreIdAndStatusAndOutOfStock(storeId, "APPROVED");

        variantStats.put("totalProducts", totalProducts);
        variantStats.put("lowStockProducts", lowStockProducts);
        variantStats.put("outOfStockProducts", outOfStockProducts);

        return variantStats;
    }
    
    @Override
    public Map<String, Object> getBestSellingVariants(String storeId, Integer limit, String period) throws Exception {
        Map<String, Object> result = new HashMap<>();

        // Lấy các đơn hàng đã giao trong khoảng thời gian
        List<Order> orders = getOrdersByPeriod(storeId, period);

        if (orders.isEmpty()) {
            result.put("variants", new ArrayList<>());
            result.put("period", period);
            result.put("limit", limit);
            return result;
        }

        // Lấy tất cả order IDs một lần
        List<String> orderIds = orders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        // Lấy tất cả order items
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdIn(orderIds);

        // Nhóm theo variant + color và tính tổng số lượng bằng Stream API
        Map<String, Integer> salesQuantityMap = orderItems.stream()
                .filter(item -> item.getProductVariant() != null)
                .collect(Collectors.groupingBy(
                        item -> {
                            String variantId = item.getProductVariant().getId();
                            String colorId = item.getColorId() != null ? item.getColorId() : "default";
                            return variantId + "_" + colorId;
                        },
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        // Tạo map chi tiết với thông tin variant (chỉ tạo cho những variant có bán)
        Map<String, Map<String, Object>> variantDetailsMap = orderItems.stream()
                .filter(item -> item.getProductVariant() != null)
                .collect(Collectors.toMap(
                        item -> {
                            String variantId = item.getProductVariant().getId();
                            String colorId = item.getColorId() != null ? item.getColorId() : "default";
                            return variantId + "_" + colorId;
                        },
                        item -> {
                            Map<String, Object> variantData = new HashMap<>();
                            ProductVariant variant = item.getProductVariant();
                            String colorId = item.getColorId() != null ? item.getColorId() : "default";
                            
                            variantData.put("variantId", variant.getId());
                            variantData.put("variantName", variant.getName());
                            variantData.put("primaryImageUrl", variant.getPrimaryImageUrl());
                            variantData.put("price", variant.getPrice());
                            variantData.put("currentStock", variant.getStock());
                            
                            // Thông tin màu sắc nếu có
                            if (!colorId.equals("default") && variant.getColors() != null) {
                                ProductVariant.ColorOption color = ProductVariant.getColor(variant, colorId);
                                if (color != null) {
                                    Map<String, Object> colorInfo = new HashMap<>();
                                    colorInfo.put("colorId", color.getId());
                                    colorInfo.put("colorName", color.getColorName());
                                    colorInfo.put("colorImage", color.getImage());
                                    colorInfo.put("colorPrice", color.getPrice());
                                    colorInfo.put("colorStock", color.getStock());
                                    variantData.put("color", colorInfo);
                                }
                            }
                            
                            return variantData;
                        },
                        (existing, replacement) -> existing
                ));

        // Kết hợp số lượng với thông tin chi tiết, sắp xếp và lấy top limit
        List<Map<String, Object>> bestSellingVariants = salesQuantityMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> variantData = variantDetailsMap.get(entry.getKey());
                    if (variantData != null) {
                        variantData.put("totalQuantity", entry.getValue());
                    }
                    return variantData;
                })
                .filter(data -> data != null)
                .sorted((v1, v2) -> Integer.compare((Integer) v2.get("totalQuantity"), (Integer) v1.get("totalQuantity")))
                .limit(limit)
                .collect(Collectors.toList());

        result.put("variants", bestSellingVariants);
        result.put("period", period);
        result.put("limit", limit);

        return result;
    }

    private List<Order> getOrdersByPeriod(String storeId, String period) {
        LocalDateTime startDate;
        LocalDateTime now = LocalDateTime.now();

        switch (period.toUpperCase()) {
            case "WEEK":
                // 7 ngày gần nhất
                startDate = now.minusDays(7)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "MONTH":
                // 30 ngày gần nhất
                startDate = now.minusDays(30)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "YEAR":
                // 365 ngày gần nhất
                startDate = now.minusDays(365)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
                break;
            case "ALL":
                return orderRepository.findByStoreIdAndStatus(storeId, "COMPLETED");
            default:
                // Default: 30 ngày gần nhất
                startDate = now.minusDays(30)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
        }

        return orderRepository.findByStoreIdAndStatusAndDateRange(storeId, "COMPLETED", startDate, now);
    }
}