package com.smartstore.domain.report.repository;

import com.smartstore.domain.report.dto.RevenueReportResponse;
import com.smartstore.domain.report.dto.TopProductResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl {

    private final EntityManager em;

    // ===== DOANH THU =====

    // ⭐ Native SQL query — dùng khi JPQL không đủ mạnh
    //    to_char() là function của PostgreSQL để format date
    @SuppressWarnings("unchecked")
    public List<RevenueReportResponse> getRevenueByDay(UUID storeId, Instant from, Instant to) {
        String sql = """
            SELECT
                TO_CHAR(o.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYYY-MM-DD') AS period,
                COALESCE(SUM(o.total_amount), 0)   AS revenue,
                COUNT(o.id)                         AS order_count,
                COALESCE(AVG(o.total_amount), 0)   AS average_order_value
            FROM orders o
            WHERE o.store_id = :storeId
              AND o.status IN ('CONFIRMED', 'COMPLETED')
              AND o.created_at BETWEEN :from AND :to
            GROUP BY TO_CHAR(o.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYYY-MM-DD')
            ORDER BY period ASC
            """;

        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList()
                .stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return RevenueReportResponse.builder()
                            .period((String) r[0])
                            .revenue((BigDecimal) r[1])
                            .orderCount(((Number) r[2]).longValue())
                            .averageOrderValue((BigDecimal) r[3])
                            .build();
                })
                .toList();
    }

    @SuppressWarnings("unchecked")
    public List<RevenueReportResponse> getRevenueByMonth(UUID storeId, int year) {
        String sql = """
            SELECT
                TO_CHAR(o.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYYY-MM') AS period,
                COALESCE(SUM(o.total_amount), 0)  AS revenue,
                COUNT(o.id)                        AS order_count,
                COALESCE(AVG(o.total_amount), 0)  AS average_order_value
            FROM orders o
            WHERE o.store_id = :storeId
              AND o.status IN ('CONFIRMED', 'COMPLETED')
              AND EXTRACT(YEAR FROM o.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh') = :year
            GROUP BY TO_CHAR(o.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYYY-MM')
            ORDER BY period ASC
            """;

        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("year", year)
                .getResultList()
                .stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return RevenueReportResponse.builder()
                            .period((String) r[0])
                            .revenue((BigDecimal) r[1])
                            .orderCount(((Number) r[2]).longValue())
                            .averageOrderValue((BigDecimal) r[3])
                            .build();
                })
                .toList();
    }

    // ===== TOP SẢN PHẨM =====

    @SuppressWarnings("unchecked")
    public List<TopProductResponse> getTopProducts(UUID storeId, Instant from, Instant to, int limit) {
        String sql = """
            SELECT
                p.id            AS product_id,
                p.name          AS product_name,
                p.sku           AS product_sku,
                SUM(oi.quantity)        AS quantity_sold,
                SUM(oi.subtotal)        AS revenue
            FROM order_items oi
            JOIN orders o   ON oi.order_id  = o.id
            JOIN products p ON oi.product_id = p.id
            WHERE o.store_id = :storeId
              AND o.status IN ('CONFIRMED', 'COMPLETED')
              AND o.created_at BETWEEN :from AND :to
            GROUP BY p.id, p.name, p.sku
            ORDER BY quantity_sold DESC
            LIMIT :limit
            """;

        return em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("limit", limit)
                .getResultList()
                .stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return new TopProductResponse(
                            UUID.fromString(r[0].toString()),
                            (String) r[1],
                            (String) r[2],
                            ((Number) r[3]).longValue(),
                            (BigDecimal) r[4]
                    );
                })
                .toList();
    }

    // ===== DASHBOARD SUMMARY =====

    public BigDecimal getRevenueBetween(UUID storeId, Instant from, Instant to) {
        String sql = """
            SELECT COALESCE(SUM(total_amount), 0)
            FROM orders
            WHERE store_id = :storeId
              AND status IN ('CONFIRMED', 'COMPLETED')
              AND created_at BETWEEN :from AND :to
            """;

        Object result = em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return (BigDecimal) result;
    }

    public Long getOrderCountBetween(UUID storeId, Instant from, Instant to) {
        String sql = """
            SELECT COUNT(*)
            FROM orders
            WHERE store_id = :storeId
              AND status IN ('CONFIRMED', 'COMPLETED')
              AND created_at BETWEEN :from AND :to
            """;

        Object result = em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        return ((Number) result).longValue();
    }

    public Long getLowStockCount(UUID storeId) {
        String sql = """
            SELECT COUNT(*)
            FROM products
            WHERE store_id = :storeId
              AND is_active = true
              AND stock_quantity <= min_stock_level
              AND stock_quantity > 0
            """;

        Object result = em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .getSingleResult();

        return ((Number) result).longValue();
    }

    public Long getOutOfStockCount(UUID storeId) {
        String sql = """
            SELECT COUNT(*)
            FROM products
            WHERE store_id = :storeId
              AND is_active = true
              AND stock_quantity = 0
            """;

        Object result = em.createNativeQuery(sql)
                .setParameter("storeId", storeId)
                .getSingleResult();

        return ((Number) result).longValue();
    }
}