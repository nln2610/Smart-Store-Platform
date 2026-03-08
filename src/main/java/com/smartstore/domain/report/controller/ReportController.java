// ReportController.java
package com.smartstore.domain.report.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.report.dto.*;
import com.smartstore.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
// ⭐ Đặt @PreAuthorize ở class level — áp dụng cho TẤT CẢ methods
//    Không cần lặp lại ở từng method nữa
public class ReportController {

    private final ReportService reportService;

    // Tổng quan dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboard(
            @RequestParam UUID storeId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<DashboardSummaryResponse>success(
                        reportService.getDashboardSummary(storeId)));
    }

    // Doanh thu theo ngày
    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<List<RevenueReportResponse>>> getDailyRevenue(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
            // ⭐ @DateTimeFormat: Postman gửi "2024-03-01", Spring tự parse thành LocalDate
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<RevenueReportResponse>>success(
                        reportService.getRevenueByDay(storeId, from, to)));
    }

    // Doanh thu theo tháng
    @GetMapping("/revenue/monthly")
    public ResponseEntity<ApiResponse<List<RevenueReportResponse>>> getMonthlyRevenue(
            @RequestParam UUID storeId,
            @RequestParam(defaultValue = "2024") int year
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<RevenueReportResponse>>success(
                        reportService.getRevenueByMonth(storeId, year)));
    }

    // Top sản phẩm bán chạy
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<List<TopProductResponse>>> getTopProducts(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<TopProductResponse>>success(
                        reportService.getTopProducts(storeId, from, to, limit)));
    }
}