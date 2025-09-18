package com.example.e_commerce_techshop.responses;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsResponse {
    private String storeId;
    private LocalDateTime period;
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Double averageOrderValue;
    private Long totalProducts;
    private Long totalReviews;
    private Double averageRating;
    
    // Revenue analytics
    private BigDecimal dailyRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
    private Map<String, BigDecimal> revenueByPeriod;
    
    // Order analytics
    private Long pendingOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    
    // Product analytics
    private Long lowStockProducts;
    private Long outOfStockProducts;
    private List<TopProductResponse> topProducts;
    
    // Customer analytics
    private Long newCustomers;
    private Long returningCustomers;
    private List<TopCustomerResponse> topCustomers;
    
    // Review analytics
    private Long pendingReviews;
    private Map<Integer, Long> reviewsByRating;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class TopProductResponse {
    private String productId;
    private String productName;
    private Long totalSold;
    private BigDecimal totalRevenue;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class TopCustomerResponse {
    private String customerId;
    private String customerName;
    private Long totalOrders;
    private BigDecimal totalSpent;
}
