package com.example.e_commerce_techshop.services.statistics;

import java.util.Map;

public interface IStatisticsService {
    Map<String, Object> getAdminOverviewStatistics() throws Exception;

    Map<String, Object> getAdminRevenueStatistics();

    Map<String, Object> getAdminServiceFees(int page, int size);

    Map<String, Object> getAdminPlatformDiscountLosses(int page, int size);

    Map<String, Object> getAdminRevenueByDateRange(String startDate, String endDate, int page, int size);

    // Chart data methods
    Map<String, Object> getRevenueChartData(String period);

    Map<String, Object> getStoreOverviewStatistics(String storeId);

    Map<String, Object> getStoreRevenueChartData(String storeId, String period) throws Exception;

    Map<String, Object> getStoreOrdersChartData(String storeId, String period) throws Exception;

    Map<String, Object> getOrderCountByStatus(String storeId) throws Exception;

    Map<String, Object> getVariantCountByStockStatus(String storeId) throws Exception;
}
