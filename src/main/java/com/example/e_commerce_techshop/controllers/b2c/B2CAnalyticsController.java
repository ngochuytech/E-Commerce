package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.AnalyticsResponse;
import com.example.e_commerce_techshop.services.analytics.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/b2c/analytics")
@RequiredArgsConstructor
@Tag(name = "B2C Analytics", description = "Analytics APIs for B2C store management - Provides comprehensive analytics and reporting features for store owners")
@SecurityRequirement(name = "bearerAuth")
public class B2CAnalyticsController {

    private final IAnalyticsService analyticsService;

    // Dashboard analytics
    @GetMapping("/dashboard/{storeId}")
    @Operation(summary = "Get dashboard analytics", description = "Retrieve comprehensive dashboard analytics for a store including revenue, orders, products, and customer metrics")
    public ResponseEntity<?> getDashboardAnalytics(
            @Parameter(description = "ID of the store to get analytics for", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        AnalyticsResponse analytics = analyticsService.getDashboardAnalytics(storeId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Revenue analytics
    @GetMapping("/revenue/{storeId}")
    @Operation(summary = "Get revenue analytics", description = "Retrieve revenue analytics for a specific time period (daily, weekly, monthly, yearly)")
    public ResponseEntity<?> getRevenueAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Time period for analytics", required = true, example = "monthly") @RequestParam String period)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getRevenueAnalytics(storeId, period);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Revenue by date range
    @GetMapping("/revenue/{storeId}/date-range")
    @Operation(summary = "Get revenue by date range", description = "Retrieve revenue analytics for a custom date range")
    public ResponseEntity<?> getRevenueByDateRange(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2024-01-01") @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2024-12-31") @RequestParam String endDate)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getRevenueByDateRange(storeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Order analytics
    @GetMapping("/orders/{storeId}")
    @Operation(summary = "Get order analytics", description = "Retrieve order statistics and trends for a specific time period")
    public ResponseEntity<?> getOrderAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Time period for analytics", required = true, example = "weekly") @RequestParam String period)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getOrderAnalytics(storeId, period);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Order status analytics
    @GetMapping("/orders/{storeId}/status")
    @Operation(summary = "Get order status analytics", description = "Retrieve analytics about order status distribution (pending, confirmed, shipped, delivered, cancelled)")
    public ResponseEntity<?> getOrderStatusAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getOrderStatusAnalytics(storeId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Product analytics
    @GetMapping("/products/{storeId}")
    @Operation(summary = "Get product analytics", description = "Retrieve comprehensive product analytics including total products, views, sales performance")
    public ResponseEntity<?> getProductAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getProductAnalytics(storeId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Top products
    @GetMapping("/products/{storeId}/top")
    @Operation(summary = "Get top products", description = "Retrieve top-selling products based on sales volume or revenue")
    public ResponseEntity<?> getTopProducts(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Number of top products to retrieve", example = "10") @RequestParam(defaultValue = "10") int limit)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getTopProducts(storeId, limit);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Inventory analytics
    @GetMapping("/inventory/{storeId}")
    @Operation(summary = "Get inventory analytics", description = "Retrieve inventory analytics including stock levels, low stock alerts, and inventory turnover")
    public ResponseEntity<?> getInventoryAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getInventoryAnalytics(storeId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Customer analytics
    @GetMapping("/customers/{storeId}")
    @Operation(summary = "Get customer analytics", description = "Retrieve customer analytics including total customers, new customers, and customer behavior metrics")
    public ResponseEntity<?> getCustomerAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Time period for analytics", required = true, example = "monthly") @RequestParam String period)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getCustomerAnalytics(storeId, period);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Top customers
    @GetMapping("/customers/{storeId}/top")
    @Operation(summary = "Get top customers", description = "Retrieve top customers based on purchase amount or order frequency")
    public ResponseEntity<?> getTopCustomers(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Number of top customers to retrieve", example = "10") @RequestParam(defaultValue = "10") int limit)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getTopCustomers(storeId, limit);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Customer growth
    @GetMapping("/customers/{storeId}/growth")
    @Operation(summary = "Get customer growth analytics", description = "Retrieve customer growth trends and acquisition metrics over time")
    public ResponseEntity<?> getCustomerGrowthAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Time period for growth analytics", required = true, example = "quarterly") @RequestParam String period)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getCustomerGrowthAnalytics(storeId, period);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Review analytics
    @GetMapping("/reviews/{storeId}")
    @Operation(summary = "Get review analytics", description = "Retrieve comprehensive review analytics including average ratings, total reviews, and review trends")
    public ResponseEntity<?> getReviewAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getReviewAnalytics(storeId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Rating distribution
    @GetMapping("/reviews/{storeId}/rating-distribution")
    @Operation(summary = "Get rating distribution", description = "Retrieve distribution of ratings (1-5 stars) for store products")
    public ResponseEntity<?> getRatingDistribution(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getRatingDistribution(storeId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Pending reviews analytics
    @GetMapping("/reviews/{storeId}/pending")
    @Operation(summary = "Get pending reviews analytics", description = "Retrieve analytics about pending reviews that need store owner response")
    public ResponseEntity<?> getPendingReviewsAnalytics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getPendingReviewsAnalytics(storeId);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Sales trend
    @GetMapping("/sales/{storeId}/trend")
    @Operation(summary = "Get sales trend", description = "Retrieve sales trend analysis showing growth patterns and seasonal variations")
    public ResponseEntity<?> getSalesTrend(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Time period for trend analysis", required = true, example = "yearly") @RequestParam String period)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getSalesTrend(storeId, period);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Sales by category
    // Doanh thu theo danh mục
    @GetMapping("/sales/{storeId}/category")
    @Operation(summary = "Get sales by category", description = "Retrieve sales breakdown by product categories to identify top-performing categories")
    public ResponseEntity<?> getSalesByCategory(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Time period for category analysis", required = true, example = "monthly") @RequestParam String period)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getSalesByCategory(storeId, period);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }

    // Performance metrics
    // Lấy chỉ số hiệu suất
    @GetMapping("/performance/{storeId}")
    @Operation(summary = "Get performance metrics", description = "Retrieve comprehensive performance metrics including KPIs, conversion rates, and efficiency indicators")
    public ResponseEntity<?> getPerformanceMetrics(
            @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1") @PathVariable String storeId,
            @Parameter(description = "Time period for performance analysis", required = true, example = "quarterly") @RequestParam String period)
            throws Exception {
        Map<String, Object> analytics = analyticsService.getPerformanceMetrics(storeId, period);
        return ResponseEntity.ok(ApiResponse.ok(analytics));
    }
}
