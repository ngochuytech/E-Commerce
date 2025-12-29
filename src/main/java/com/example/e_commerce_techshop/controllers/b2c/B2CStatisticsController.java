package com.example.e_commerce_techshop.controllers.b2c;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.models.Store;
import com.example.e_commerce_techshop.models.User;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.statistics.IStatisticsService;
import com.example.e_commerce_techshop.services.store.IStoreService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/b2c/statistics")
@RequiredArgsConstructor
@Tag(name = "Shop Statistics Management", description = "API cho quản lý thống kê của cửa hàng")
@SecurityRequirement(name = "Bearer Authentication")
public class B2CStatisticsController {
    private final IStatisticsService statisticsService;
    private final IStoreService storeService;

    private void validateUserStore(User currentUser, String storeId) {
        List<Store> userStores = storeService.getStoresByOwner(currentUser.getId());
        boolean hasStore = userStores.stream()
                .anyMatch(store -> store.getId().equals(storeId));

        if (!hasStore) {
            throw new RuntimeException("Bạn không có quyền truy cập cửa hàng này");
        }
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverviewStatistics(
            @Parameter(description = "ID của shop", required = true, example = "shop_001") @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser)
            throws Exception {
        validateUserStore(currentUser, storeId);

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getStoreOverviewStatistics(storeId)));
    }

    @GetMapping("/revenue/chart-data")
    public ResponseEntity<?> getRevenueChartData(
            @Parameter(description = "ID của shop", required = true, example = "shop_001") @RequestParam String storeId,
            @Parameter(description = "Kỳ thời gian (WEEK, MONTH, YEAR)", required = true, example = "MONTH") @RequestParam String period,
            @AuthenticationPrincipal User currentUser)
            throws Exception {
        validateUserStore(currentUser, storeId);

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getStoreRevenueChartData(storeId, period)));
    }

    @GetMapping("/orders/chart-data")
    public ResponseEntity<?> getOrdersChartData(
            @Parameter(description = "ID của shop", required = true, example = "shop_001") @RequestParam String storeId,
            @Parameter(description = "Kỳ thời gian (WEEK, MONTH, YEAR)", required = true, example = "MONTH") @RequestParam String period,
            @AuthenticationPrincipal User currentUser)
            throws Exception {
        validateUserStore(currentUser, storeId);

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getStoreOrdersChartData(storeId, period)));
    }

    @GetMapping("/orders/count-by-status")
    public ResponseEntity<?> getOrderCountByStatus(
            @Parameter(description = "ID của shop", required = true, example = "shop_001") @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser)
            throws Exception {
        validateUserStore(currentUser, storeId);

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getOrderCountByStatus(storeId)));
    }

    @GetMapping("/variant/count-by-stock-status")
    public ResponseEntity<?> getVariantCountByStockStatus(
            @Parameter(description = "ID của shop", required = true, example = "shop_001") @RequestParam String storeId,
            @AuthenticationPrincipal User currentUser)
            throws Exception {
        validateUserStore(currentUser, storeId);

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getVariantCountByStockStatus(storeId)));
    }

    @GetMapping("/variants/best-selling")
    public ResponseEntity<?> getBestSellingVariants(
            @Parameter(description = "ID của shop", required = true, example = "shop_001") @RequestParam String storeId,
            @Parameter(description = "Số lượng variant muốn lấy", required = false, example = "10") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "Kỳ thời gian (WEEK, MONTH, YEAR, ALL)", required = false, example = "MONTH") @RequestParam(defaultValue = "MONTH") String period,
            @AuthenticationPrincipal User currentUser)
            throws Exception {
        validateUserStore(currentUser, storeId);
        Map<String, Object> result = statisticsService.getBestSellingVariants(storeId, limit, period);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
