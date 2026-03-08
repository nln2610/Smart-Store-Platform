// ReportService.java
package com.smartstore.domain.report.service;

import com.smartstore.domain.report.dto.*;
import com.smartstore.domain.report.repository.ReportRepositoryImpl;
import com.smartstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepositoryImpl reportRepository;
    private final ProductRepository productRepository;

    // ⭐ Hằng số timezone — đặt ở đây để dễ thay đổi sau
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public DashboardSummaryResponse getDashboardSummary(UUID storeId) {
        // Tính khoảng thời gian hôm nay (00:00 → 23:59 theo giờ VN)
        LocalDate today = LocalDate.now(VN_ZONE);
        Instant startOfDay  = today.atStartOfDay(VN_ZONE).toInstant();
        Instant endOfDay    = today.plusDays(1).atStartOfDay(VN_ZONE).toInstant();

        // Tính khoảng thời gian tháng này
        Instant startOfMonth = today.withDayOfMonth(1).atStartOfDay(VN_ZONE).toInstant();

        return DashboardSummaryResponse.builder()
                // Hôm nay
                .revenueToday(reportRepository.getRevenueBetween(storeId, startOfDay, endOfDay))
                .ordersToday(reportRepository.getOrderCountBetween(storeId, startOfDay, endOfDay))

                // Tháng này
                .revenueThisMonth(reportRepository.getRevenueBetween(storeId, startOfMonth, endOfDay))
                .ordersThisMonth(reportRepository.getOrderCountBetween(storeId, startOfMonth, endOfDay))

                // Tồn kho
                .totalProducts(productRepository.countByStoreIdAndIsActiveTrue(storeId))
                .lowStockProducts(reportRepository.getLowStockCount(storeId))
                .outOfStockProducts(reportRepository.getOutOfStockCount(storeId))
                .build();
    }

    public List<RevenueReportResponse> getRevenueByDay(UUID storeId, LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(VN_ZONE).toInstant();
        Instant toInstant   = to.plusDays(1).atStartOfDay(VN_ZONE).toInstant();
        return reportRepository.getRevenueByDay(storeId, fromInstant, toInstant);
    }

    public List<RevenueReportResponse> getRevenueByMonth(UUID storeId, int year) {
        return reportRepository.getRevenueByMonth(storeId, year);
    }

    public List<TopProductResponse> getTopProducts(
            UUID storeId, LocalDate from, LocalDate to, int limit
    ) {
        Instant fromInstant = from.atStartOfDay(VN_ZONE).toInstant();
        Instant toInstant   = to.plusDays(1).atStartOfDay(VN_ZONE).toInstant();
        return reportRepository.getTopProducts(storeId, fromInstant, toInstant, limit);
    }
}