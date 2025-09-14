package com.example.e_commerce_techshop.controllers.b2c.analytics;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.AnalyticsResponse;
import com.example.e_commerce_techshop.services.analytics.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/b2c/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final IAnalyticsService analyticsService;
    
    // Dashboard analytics
    @GetMapping("/dashboard/{storeId}")
    public ResponseEntity<?> getDashboardAnalytics(@PathVariable String storeId) {
        try {
            AnalyticsResponse analytics = analyticsService.getDashboardAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê tổng quan thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Revenue analytics
    @GetMapping("/revenue/{storeId}")
    public ResponseEntity<?> getRevenueAnalytics(@PathVariable String storeId, @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getRevenueAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê doanh thu thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Revenue by date range
    @GetMapping("/revenue/{storeId}/date-range")
    public ResponseEntity<?> getRevenueByDateRange(@PathVariable String storeId, 
                                                  @RequestParam String startDate, 
                                                  @RequestParam String endDate) {
        try {
            Map<String, Object> analytics = analyticsService.getRevenueByDateRange(storeId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.ok("Lấy doanh thu theo khoảng thời gian thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Order analytics
    @GetMapping("/orders/{storeId}")
    public ResponseEntity<?> getOrderAnalytics(@PathVariable String storeId, @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getOrderAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê đơn hàng thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Order status analytics
    @GetMapping("/orders/{storeId}/status")
    public ResponseEntity<?> getOrderStatusAnalytics(@PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getOrderStatusAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê trạng thái đơn hàng thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Product analytics
    @GetMapping("/products/{storeId}")
    public ResponseEntity<?> getProductAnalytics(@PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getProductAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê sản phẩm thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Top products
    @GetMapping("/products/{storeId}/top")
    public ResponseEntity<?> getTopProducts(@PathVariable String storeId, @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> analytics = analyticsService.getTopProducts(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok("Lấy sản phẩm bán chạy nhất thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Inventory analytics
    @GetMapping("/inventory/{storeId}")
    public ResponseEntity<?> getInventoryAnalytics(@PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getInventoryAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê tồn kho thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Customer analytics
    @GetMapping("/customers/{storeId}")
    public ResponseEntity<?> getCustomerAnalytics(@PathVariable String storeId, @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getCustomerAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê khách hàng thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Top customers
    @GetMapping("/customers/{storeId}/top")
    public ResponseEntity<?> getTopCustomers(@PathVariable String storeId, @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> analytics = analyticsService.getTopCustomers(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok("Lấy khách hàng chi tiêu cao nhất thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Customer growth
    @GetMapping("/customers/{storeId}/growth")
    public ResponseEntity<?> getCustomerGrowthAnalytics(@PathVariable String storeId, @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getCustomerGrowthAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê tăng trưởng khách hàng thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Review analytics
    @GetMapping("/reviews/{storeId}")
    public ResponseEntity<?> getReviewAnalytics(@PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getReviewAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê đánh giá thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Rating distribution
    @GetMapping("/reviews/{storeId}/rating-distribution")
    public ResponseEntity<?> getRatingDistribution(@PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getRatingDistribution(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy phân bố đánh giá thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Pending reviews analytics
    @GetMapping("/reviews/{storeId}/pending")
    public ResponseEntity<?> getPendingReviewsAnalytics(@PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getPendingReviewsAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok("Lấy thống kê đánh giá chờ phản hồi thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Sales trend
    @GetMapping("/sales/{storeId}/trend")
    public ResponseEntity<?> getSalesTrend(@PathVariable String storeId, @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getSalesTrend(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok("Lấy xu hướng bán hàng thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Sales by category
    @GetMapping("/sales/{storeId}/category")
    public ResponseEntity<?> getSalesByCategory(@PathVariable String storeId, @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getSalesByCategory(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok("Lấy doanh thu theo danh mục thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Performance metrics
    @GetMapping("/performance/{storeId}")
    public ResponseEntity<?> getPerformanceMetrics(@PathVariable String storeId, @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getPerformanceMetrics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok("Lấy chỉ số hiệu suất thành công!", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}



