package com.example.e_commerce_techshop.controllers.b2c;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.AnalyticsResponse;
import com.example.e_commerce_techshop.services.analytics.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "Get dashboard analytics",
        description = "Retrieve comprehensive dashboard analytics for a store including revenue, orders, products, and customer metrics"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Dashboard analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = AnalyticsResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid store ID",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getDashboardAnalytics(
        @Parameter(description = "ID of the store to get analytics for", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            AnalyticsResponse analytics = analyticsService.getDashboardAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Revenue analytics
    @GetMapping("/revenue/{storeId}")
    @Operation(
        summary = "Get revenue analytics",
        description = "Retrieve revenue analytics for a specific time period (daily, weekly, monthly, yearly)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Revenue analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getRevenueAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Time period for analytics", required = true, example = "monthly")
        @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getRevenueAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Revenue by date range
    @GetMapping("/revenue/{storeId}/date-range")
    @Operation(
        summary = "Get revenue by date range",
        description = "Retrieve revenue analytics for a custom date range"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Revenue analytics by date range retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid date format or range",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getRevenueByDateRange(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2024-01-01")
        @RequestParam String startDate, 
        @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2024-12-31")
        @RequestParam String endDate) {
        try {
            Map<String, Object> analytics = analyticsService.getRevenueByDateRange(storeId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Order analytics
    @GetMapping("/orders/{storeId}")
    @Operation(
        summary = "Get order analytics",
        description = "Retrieve order statistics and trends for a specific time period"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Order analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getOrderAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Time period for analytics", required = true, example = "weekly")
        @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getOrderAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Order status analytics
    @GetMapping("/orders/{storeId}/status")
    @Operation(
        summary = "Get order status analytics",
        description = "Retrieve analytics about order status distribution (pending, confirmed, shipped, delivered, cancelled)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Order status analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid store ID",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getOrderStatusAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getOrderStatusAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Product analytics
    @GetMapping("/products/{storeId}")
    @Operation(
        summary = "Get product analytics",
        description = "Retrieve comprehensive product analytics including total products, views, sales performance"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Product analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid store ID",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getProductAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getProductAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Top products
    @GetMapping("/products/{storeId}/top")
    @Operation(
        summary = "Get top products",
        description = "Retrieve top-selling products based on sales volume or revenue"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Top products retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getTopProducts(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Number of top products to retrieve", example = "10")
        @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> analytics = analyticsService.getTopProducts(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Inventory analytics
    @GetMapping("/inventory/{storeId}")
    @Operation(
        summary = "Get inventory analytics",
        description = "Retrieve inventory analytics including stock levels, low stock alerts, and inventory turnover"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Inventory analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid store ID",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getInventoryAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getInventoryAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Customer analytics
    @GetMapping("/customers/{storeId}")
    @Operation(
        summary = "Get customer analytics",
        description = "Retrieve customer analytics including total customers, new customers, and customer behavior metrics"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Customer analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getCustomerAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Time period for analytics", required = true, example = "monthly")
        @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getCustomerAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Top customers
    @GetMapping("/customers/{storeId}/top")
    @Operation(
        summary = "Get top customers",
        description = "Retrieve top customers based on purchase amount or order frequency"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Top customers retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getTopCustomers(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Number of top customers to retrieve", example = "10")
        @RequestParam(defaultValue = "10") int limit) {
        try {
            Map<String, Object> analytics = analyticsService.getTopCustomers(storeId, limit);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Customer growth
    @GetMapping("/customers/{storeId}/growth")
    @Operation(
        summary = "Get customer growth analytics",
        description = "Retrieve customer growth trends and acquisition metrics over time"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Customer growth analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getCustomerGrowthAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Time period for growth analytics", required = true, example = "quarterly")
        @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getCustomerGrowthAnalytics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Review analytics
    @GetMapping("/reviews/{storeId}")
    @Operation(
        summary = "Get review analytics",
        description = "Retrieve comprehensive review analytics including average ratings, total reviews, and review trends"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Review analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid store ID",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getReviewAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getReviewAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Rating distribution
    @GetMapping("/reviews/{storeId}/rating-distribution")
    @Operation(
        summary = "Get rating distribution",
        description = "Retrieve distribution of ratings (1-5 stars) for store products"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Rating distribution retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid store ID",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getRatingDistribution(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getRatingDistribution(storeId);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Pending reviews analytics
    @GetMapping("/reviews/{storeId}/pending")
    @Operation(
        summary = "Get pending reviews analytics",
        description = "Retrieve analytics about pending reviews that need store owner response"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Pending reviews analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid store ID",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getPendingReviewsAnalytics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId) {
        try {
            Map<String, Object> analytics = analyticsService.getPendingReviewsAnalytics(storeId);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Sales trend
    @GetMapping("/sales/{storeId}/trend")
    @Operation(
        summary = "Get sales trend",
        description = "Retrieve sales trend analysis showing growth patterns and seasonal variations"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Sales trend retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getSalesTrend(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Time period for trend analysis", required = true, example = "yearly")
        @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getSalesTrend(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Sales by category
    // Doanh thu theo danh mục
    @GetMapping("/sales/{storeId}/category")
    @Operation(
        summary = "Get sales by category",
        description = "Retrieve sales breakdown by product categories to identify top-performing categories"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Sales by category retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getSalesByCategory(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Time period for category analysis", required = true, example = "monthly")
        @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getSalesByCategory(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Performance metrics
    // Lấy chỉ số hiệu suất
    @GetMapping("/performance/{storeId}")
    @Operation(
        summary = "Get performance metrics",
        description = "Retrieve comprehensive performance metrics including KPIs, conversion rates, and efficiency indicators"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Performance metrics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Bad request - invalid parameters",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<?> getPerformanceMetrics(
        @Parameter(description = "ID of the store", required = true, example = "64f1a2b3c4d5e6f7a8b9c0d1")
        @PathVariable String storeId, 
        @Parameter(description = "Time period for performance analysis", required = true, example = "quarterly")
        @RequestParam String period) {
        try {
            Map<String, Object> analytics = analyticsService.getPerformanceMetrics(storeId, period);
            return ResponseEntity.ok(ApiResponse.ok(analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}



