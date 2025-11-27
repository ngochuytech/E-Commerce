package com.example.e_commerce_techshop.controllers.admin;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.services.statistics.IStatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "Admin Statistics Management", description = "APIs for admin to view revenue statistics")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {
    private final IStatisticsService statisticsService;

    @GetMapping("/overview")
    public ResponseEntity<?> getOverviewStatistics() throws Exception {
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getAdminOverviewStatistics()));
    }

    @Operation(summary = "Lấy thống kê doanh thu", description = "Lấy tổng phí dịch vụ và tổng tiền lỗ từ các chương trình giảm giá nền tảng")
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenueStatistics() {
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getAdminRevenueStatistics()));
    }

    @Operation(summary = "Get service fees", description = "Retrieve all service fee records")
    @GetMapping("/service-fees")
    public ResponseEntity<?> getServiceFees(
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getAdminServiceFees(pageable)));
    }

    @Operation(summary = "Get platform discount losses", description = "Retrieve all platform discount loss records")
    @GetMapping("/platform-discount-losses")
    public ResponseEntity<?> getPlatformDiscountLosses(
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getAdminPlatformDiscountLosses(pageable)));
    }

    @Operation(summary = "Get revenue by date range", description = "Get service fees collected in a specific date range")
    @GetMapping("/date-range")
    public ResponseEntity<?> getRevenueByDateRange(
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true, example = "2025-11-01") @RequestParam String startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true, example = "2025-11-30") @RequestParam String endDate,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity
                .ok(ApiResponse.ok(statisticsService.getAdminRevenueByDateRange(startDate, endDate, pageable)));
    }

    @Operation(summary = "Get revenue chart data", description = "Get revenue data for chart visualization by period (WEEK/MONTH/YEAR)")
    @GetMapping("/chart-data")
    public ResponseEntity<?> getRevenueChartData(
            @Parameter(description = "Period: WEEK, MONTH, or YEAR", required = true, example = "MONTH") @RequestParam String period) {

        return ResponseEntity.ok(ApiResponse.ok(statisticsService.getRevenueChartData(period)));
    }

}
