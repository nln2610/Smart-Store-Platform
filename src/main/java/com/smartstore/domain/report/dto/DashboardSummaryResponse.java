// DashboardSummaryResponse.java
package com.smartstore.domain.report.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardSummaryResponse {
    // Tổng quan hôm nay
    private BigDecimal revenueToday;
    private Long ordersToday;
    private Long newCustomersToday;

    // Tổng quan tháng này
    private BigDecimal revenueThisMonth;
    private Long ordersThisMonth;

    // Tồn kho
    private Long totalProducts;
    private Long lowStockProducts;    // Sản phẩm sắp hết hàng
    private Long outOfStockProducts;  // Sản phẩm hết hàng
}