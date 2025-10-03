package com.example.e_commerce_techshop.services.analytics;

import com.example.e_commerce_techshop.responses.AnalyticsResponse;

import java.util.Map;

public interface IAnalyticsService {
    
    // Dashboard analytics
    AnalyticsResponse getDashboardAnalytics(String storeId);
    
    // Revenue analytics
    Map<String, Object> getRevenueAnalytics(String storeId, String period); // daily, monthly, yearly
    Map<String, Object> getRevenueByDateRange(String storeId, String startDate, String endDate);
    
    // Order analytics
    Map<String, Object> getOrderAnalytics(String storeId, String period);
    Map<String, Object> getOrderStatusAnalytics(String storeId);
    
    // Product analytics
    Map<String, Object> getProductAnalytics(String storeId);
    Map<String, Object> getTopProducts(String storeId, int limit);
    Map<String, Object> getInventoryAnalytics(String storeId);
    
    // Customer analytics
    Map<String, Object> getCustomerAnalytics(String storeId, String period);
    Map<String, Object> getTopCustomers(String storeId, int limit);
    Map<String, Object> getCustomerGrowthAnalytics(String storeId, String period);
    
    // Review analytics
    Map<String, Object> getReviewAnalytics(String storeId);
    Map<String, Object> getRatingDistribution(String storeId);
    Map<String, Object> getPendingReviewsAnalytics(String storeId);
    
    // Sales analytics
    Map<String, Object> getSalesTrend(String storeId, String period);
    Map<String, Object> getSalesByCategory(String storeId, String period);
    
    // Performance analytics
    Map<String, Object> getPerformanceMetrics(String storeId, String period);
}



