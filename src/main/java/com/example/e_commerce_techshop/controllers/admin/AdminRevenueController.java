package com.example.e_commerce_techshop.controllers.admin;

import com.example.e_commerce_techshop.models.AdminRevenue;
import com.example.e_commerce_techshop.repositories.AdminRevenueRepository;
import com.example.e_commerce_techshop.responses.ApiResponse;
import com.example.e_commerce_techshop.responses.admin.AdminRevenueResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/admin/revenues")
@RequiredArgsConstructor
@Tag(name = "Admin Revenue Management", description = "APIs for admin to view revenue statistics")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRevenueController {

    private final AdminRevenueRepository adminRevenueRepository;

    @Operation(summary = "Lấy thống kê doanh thu", description = "Lấy tổng phí dịch vụ và tổng tiền lỗ từ các chương trình giảm giá nền tảng")
    @GetMapping("/statistics")
    public ResponseEntity<?> getRevenueStatistics() {
        // Tổng phí dịch vụ từ tất cả orders
        List<AdminRevenue> allServiceFees = adminRevenueRepository.findByRevenueType("SERVICE_FEE");
        BigDecimal totalServiceFee = allServiceFees.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền lỗ từ platform discount
        List<AdminRevenue> platformDiscountLoss = adminRevenueRepository.findByRevenueType("PLATFORM_DISCOUNT_LOSS");
        BigDecimal totalPlatformDiscountLoss = platformDiscountLoss.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalServiceFee", totalServiceFee);
        stats.put("totalPlatformDiscountLoss", totalPlatformDiscountLoss);
        stats.put("netRevenue", totalServiceFee.subtract(totalPlatformDiscountLoss));
        stats.put("serviceFeeCount", allServiceFees.size());
        stats.put("platformDiscountLossCount", platformDiscountLoss.size());

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @Operation(summary = "Get service fees", description = "Retrieve all service fee records")
    @GetMapping("/service-fees")
    public ResponseEntity<?> getServiceFees(
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size) {
        
        List<AdminRevenue> allServiceFees = adminRevenueRepository.findByRevenueType("SERVICE_FEE");
        int start = page * size;
        int end = Math.min(start + size, allServiceFees.size());
        List<AdminRevenue> paginatedFees = allServiceFees.subList(start, end);

        BigDecimal total = allServiceFees.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = paginatedFees.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("page", page);
        response.put("size", size);
        response.put("total", allServiceFees.size());
        response.put("totalAmount", total);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Get platform discount losses", description = "Retrieve all platform discount loss records")
    @GetMapping("/platform-discount-losses")
    public ResponseEntity<?> getPlatformDiscountLosses(
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size) {
        
        List<AdminRevenue> allLosses = adminRevenueRepository.findByRevenueType("PLATFORM_DISCOUNT_LOSS");
        int start = page * size;
        int end = Math.min(start + size, allLosses.size());
        List<AdminRevenue> paginatedLosses = allLosses.subList(start, end);

        BigDecimal total = allLosses.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = paginatedLosses.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("page", page);
        response.put("size", size);
        response.put("total", allLosses.size());
        response.put("totalAmount", total);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Get revenue by date range", description = "Get service fees collected in a specific date range")
    @GetMapping("/date-range")
    public ResponseEntity<?> getRevenueByDateRange(
            @Parameter(description = "Start date (format: yyyy-MM-dd)", required = true, example = "2025-11-01") @RequestParam String startDate,
            @Parameter(description = "End date (format: yyyy-MM-dd)", required = true, example = "2025-11-30") @RequestParam String endDate,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size) {

        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        List<AdminRevenue> allRevenues = adminRevenueRepository.findByCreatedAtBetween(start, end);
        int startIdx = page * size;
        int endIdx = Math.min(startIdx + size, allRevenues.size());
        List<AdminRevenue> paginatedRevenues = allRevenues.subList(startIdx, endIdx);

        BigDecimal total = allRevenues.stream()
                .map(AdminRevenue::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = paginatedRevenues.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("page", page);
        response.put("size", size);
        response.put("total", allRevenues.size());
        response.put("totalAmount", total);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Get all admin revenues", description = "Retrieve all revenue records with pagination and filtering by type")
    @GetMapping("")
    public ResponseEntity<?> getAllRevenues(
            @Parameter(description = "Filter by revenue type (SERVICE_FEE/PLATFORM_DISCOUNT_LOSS)", required = false, example = "SERVICE_FEE") @RequestParam(required = false) String revenueType,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size) {

        List<AdminRevenue> revenues;
        if (revenueType != null && !revenueType.trim().isEmpty()) {
            revenues = adminRevenueRepository.findByRevenueType(revenueType);
        } else {
            revenues = adminRevenueRepository.findAll();
        }

        int start = page * size;
        int end = Math.min(start + size, revenues.size());
        List<AdminRevenue> paginatedRevenues = revenues.subList(start, end);

        List<AdminRevenueResponse> responseList = paginatedRevenues.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("page", page);
        response.put("size", size);
        response.put("total", revenues.size());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
