package com.example.e_commerce_techshop.services.statistics;

import java.util.Map;

import org.springframework.data.domain.Pageable;

public interface IStatisticsService {
    Map<String, Object> getAdminOverviewStatistics() throws Exception;

    Map<String, Object> getAdminRevenueStatistics();

    Map<String, Object> getAdminPlatformCommissions(Pageable pageable);

    Map<String, Object> getAdminPlatformDiscountLosses(Pageable pageable);

    Map<String, Object> getAdminShippingFees(Pageable pageable);

    Map<String, Object> getAdminRevenueByDateRange(String startDate, String endDate, Pageable pageable);
    // Chart data methods
    Map<String, Object> getRevenueChartData(String period);

    Map<String, Object> getStoreOverviewStatistics(String storeId);

    Map<String, Object> getStoreRevenueChartData(String storeId, String period) throws Exception;

    Map<String, Object> getStoreOrdersChartData(String storeId, String period) throws Exception;

    Map<String, Object> getOrderCountByStatus(String storeId) throws Exception;

    Map<String, Object> getVariantCountByStockStatus(String storeId) throws Exception;

    Map<String, Object> getBestSellingVariants(String storeId, Integer limit, String period) throws Exception;
}
