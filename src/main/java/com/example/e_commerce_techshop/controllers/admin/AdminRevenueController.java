package com.example.e_commerce_techshop.controllers.admin;

import com.example.e_commerce_techshop.models.AdminRevenue;
import com.example.e_commerce_techshop.repositories.AdminRevenueRepository;
import com.example.e_commerce_techshop.responses.AdminRevenueResponse;
import com.example.e_commerce_techshop.responses.ApiResponse;
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

    @Operation(summary = "Get revenue statistics", description = "Get total, collected, and pending service fees")
    @GetMapping("/statistics")
    public ResponseEntity<?> getRevenueStatistics() {
        // Tổng phí dịch vụ từ tất cả orders (PENDING + COLLECTED)
        List<AdminRevenue> allServiceFees = adminRevenueRepository.findByRevenueType("SERVICE_FEE");
        BigDecimal totalServiceFee = allServiceFees.stream()
                .map(AdminRevenue::getServiceFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Phí dịch vụ đã thu (status = COLLECTED)
        List<AdminRevenue> collectedFees = adminRevenueRepository.findServiceFeesByStatus("COLLECTED");
        BigDecimal collectedFee = collectedFees.stream()
                .map(AdminRevenue::getServiceFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Phí dịch vụ chưa thu (status = PENDING)
        List<AdminRevenue> pendingFees = adminRevenueRepository.findServiceFeesByStatus("PENDING");
        BigDecimal pendingFee = pendingFees.stream()
                .map(AdminRevenue::getServiceFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalServiceFee", totalServiceFee);
        stats.put("collectedFee", collectedFee);
        stats.put("pendingFee", pendingFee);
        stats.put("totalCount", allServiceFees.size());
        stats.put("collectedCount", collectedFees.size());
        stats.put("pendingCount", pendingFees.size());

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @Operation(summary = "Get pending service fees", description = "Retrieve all service fees not yet collected (status = PENDING)")
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRevenue(
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", required = false, example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", required = false, example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        
        List<AdminRevenue> allPending = adminRevenueRepository.findByStatus("PENDING");
        int start = page * size;
        int end = Math.min(start + size, allPending.size());
        List<AdminRevenue> paginatedPending = allPending.subList(start, end);

        BigDecimal total = allPending.stream()
                .map(AdminRevenue::getServiceFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = paginatedPending.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("page", page);
        response.put("size", size);
        response.put("total", allPending.size());
        response.put("totalAmount", total);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Get collected service fees", description = "Retrieve all service fees already collected (status = COLLECTED)")
    @GetMapping("/collected")
    public ResponseEntity<?> getCollectedRevenue(
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", required = false, example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", required = false, example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {
        
        List<AdminRevenue> allCollected = adminRevenueRepository.findByStatus("COLLECTED");
        int start = page * size;
        int end = Math.min(start + size, allCollected.size());
        List<AdminRevenue> paginatedCollected = allCollected.subList(start, end);

        BigDecimal total = allCollected.stream()
                .map(AdminRevenue::getServiceFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminRevenueResponse> responseList = paginatedCollected.stream()
                .map(AdminRevenueResponse::fromAdminRevenue)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("revenues", responseList);
        response.put("page", page);
        response.put("size", size);
        response.put("total", allCollected.size());
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
                .map(AdminRevenue::getServiceFee)
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

    @Operation(summary = "Get all admin revenues", description = "Retrieve all service fee records with pagination and sorting")
    @GetMapping("")
    public ResponseEntity<?> getAllRevenues(
            @Parameter(description = "Filter by status (PENDING/COLLECTED)", required = false, example = "PENDING") @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-indexed)", required = false, example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false, example = "10") @RequestParam(defaultValue = "10") int size) {

        List<AdminRevenue> revenues;
        if (status != null && !status.trim().isEmpty()) {
            revenues = adminRevenueRepository.findByStatus(status);
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
