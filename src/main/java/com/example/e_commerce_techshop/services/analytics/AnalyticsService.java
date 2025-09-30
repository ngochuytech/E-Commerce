package com.example.e_commerce_techshop.services.analytics;

import com.example.e_commerce_techshop.models.Order;
import com.example.e_commerce_techshop.models.Review;
import com.example.e_commerce_techshop.repositories.OrderRepository;
import com.example.e_commerce_techshop.repositories.ReviewRepository;
import com.example.e_commerce_techshop.responses.AnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService implements IAnalyticsService {
    
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    
    @Override
    public AnalyticsResponse getDashboardAnalytics(String storeId) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        
        // Calculate basic metrics
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalOrders = (long) orders.size();
        Long totalReviews = reviewRepository.countByStoreId(storeId);
        Double averageRating = reviewRepository.getAverageRatingByStoreId(storeId);
        
        return AnalyticsResponse.builder()
                .storeId(storeId)
                .period(LocalDateTime.now())
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .build();
    }
    
    @Override
    public Map<String, Object> getRevenueAnalytics(String storeId, String period) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        Map<String, Object> result = new HashMap<>();
        
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        result.put("totalRevenue", totalRevenue);
        result.put("period", period);
        result.put("orderCount", orders.size());
        
        if (!orders.isEmpty()) {
            BigDecimal averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(orders.size()));
            result.put("averageOrderValue", averageOrderValue);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getRevenueByDateRange(String storeId, String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<Order> orders = orderRepository.findByStoreIdAndDateRange(storeId, start, end);
        Map<String, Object> result = new HashMap<>();
        
        BigDecimal revenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        result.put("revenue", revenue);
        result.put("orderCount", orders.size());
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getOrderAnalytics(String storeId, String period) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        Map<String, Object> result = new HashMap<>();
        
        Long pending = orderRepository.countByStoreIdAndStatus(storeId, "PENDING");
        Long processing = orderRepository.countByStoreIdAndStatus(storeId, "PROCESSING");
        Long shipped = orderRepository.countByStoreIdAndStatus(storeId, "SHIPPED");
        Long delivered = orderRepository.countByStoreIdAndStatus(storeId, "DELIVERED");
        Long cancelled = orderRepository.countByStoreIdAndStatus(storeId, "CANCELLED");
        
        result.put("totalOrders", orders.size());
        result.put("pending", pending);
        result.put("processing", processing);
        result.put("shipped", shipped);
        result.put("delivered", delivered);
        result.put("cancelled", cancelled);
        result.put("period", period);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getOrderStatusAnalytics(String storeId) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("pending", orderRepository.countByStoreIdAndStatus(storeId, "PENDING"));
        result.put("processing", orderRepository.countByStoreIdAndStatus(storeId, "PROCESSING"));
        result.put("shipped", orderRepository.countByStoreIdAndStatus(storeId, "SHIPPED"));
        result.put("delivered", orderRepository.countByStoreIdAndStatus(storeId, "DELIVERED"));
        result.put("cancelled", orderRepository.countByStoreIdAndStatus(storeId, "CANCELLED"));
        
        return result;
    }
    
    @Override
    public Map<String, Object> getProductAnalytics(String storeId) {
        Map<String, Object> result = new HashMap<>();
        // This would require ProductRepository - simplified for now
        result.put("totalProducts", 0);
        result.put("activeProducts", 0);
        result.put("lowStockProducts", 0);
        result.put("outOfStockProducts", 0);
        return result;
    }
    
    @Override
    public Map<String, Object> getTopProducts(String storeId, int limit) {
        Map<String, Object> result = new HashMap<>();
        // This would require complex query - simplified for now
        result.put("topProducts", "Data not available");
        result.put("limit", limit);
        return result;
    }
    
    @Override
    public Map<String, Object> getInventoryAnalytics(String storeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalVariants", 0);
        result.put("lowStock", 0);
        result.put("outOfStock", 0);
        return result;
    }
    
    @Override
    public Map<String, Object> getCustomerAnalytics(String storeId, String period) {
        List<Order> orders = orderRepository.findByStoreId(storeId);
        long uniqueCustomers = orders.stream()
                .map(order -> order.getBuyer().getId())
                .distinct()
                .count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCustomers", uniqueCustomers);
        result.put("period", period);
        return result;
    }
    
    @Override
    public Map<String, Object> getTopCustomers(String storeId, int limit) {
        Map<String, Object> result = new HashMap<>();
        result.put("topCustomers", "Data not available");
        result.put("limit", limit);
        return result;
    }
    
    @Override
    public Map<String, Object> getCustomerGrowthAnalytics(String storeId, String period) {
        Map<String, Object> result = new HashMap<>();
        result.put("newCustomers", 0);
        result.put("returningCustomers", 0);
        result.put("period", period);
        return result;
    }
    
    @Override
    public Map<String, Object> getReviewAnalytics(String storeId) {
        Map<String, Object> result = new HashMap<>();
        
        Long totalReviews = reviewRepository.countByStoreId(storeId);
        Double averageRating = reviewRepository.getAverageRatingByStoreId(storeId);
        // Seller Response ko có trong DB
//        Long pendingReviews = (long) reviewRepository.findPendingReviewsByStoreId(storeId).size();
        
        result.put("totalReviews", totalReviews);
        result.put("averageRating", averageRating);
//        result.put("pendingReviews", pendingReviews);
        
        return result;
    }
    
    @Override
    public Map<String, Object> getRatingDistribution(String storeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("5star", reviewRepository.findByRating(5).size());
        result.put("4star", reviewRepository.findByRating(4).size());
        result.put("3star", reviewRepository.findByRating(3).size());
        result.put("2star", reviewRepository.findByRating(2).size());
        result.put("1star", reviewRepository.findByRating(1).size());
        return result;
    }
    
    @Override
    public Map<String, Object> getPendingReviewsAnalytics(String storeId) {
        // Seller Response ko có trong DB
//        List<Review> pendingReviews = reviewRepository.findPendingReviewsByStoreId(storeId);
        Map<String, Object> result = new HashMap<>();
//        result.put("pendingCount", pendingReviews.size());
//        result.put("pendingReviews", pendingReviews);
        return result;
    }
    
    @Override
    public Map<String, Object> getSalesTrend(String storeId, String period) {
        Map<String, Object> result = new HashMap<>();
        result.put("trend", "Data not available");
        result.put("period", period);
        return result;
    }
    
    @Override
    public Map<String, Object> getSalesByCategory(String storeId, String period) {
        Map<String, Object> result = new HashMap<>();
        result.put("categorySales", "Data not available");
        result.put("period", period);
        return result;
    }
    
    @Override
    public Map<String, Object> getPerformanceMetrics(String storeId, String period) {
        Map<String, Object> result = new HashMap<>();
        result.put("conversionRate", 0.0);
        result.put("averageOrderValue", 0.0);
        result.put("customerRetentionRate", 0.0);
        result.put("period", period);
        return result;
    }
}
