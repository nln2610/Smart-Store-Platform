// RecommendationRepositoryImpl.java
package com.smartstore.domain.recommendation.repository;

import com.smartstore.domain.recommendation.dto.RecommendationResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RecommendationRepositoryImpl {

    private final EntityManager em;

    // =========================================================
    // LOẠI 1: FREQUENTLY BOUGHT TOGETHER
    // Tìm sản phẩm thường được mua cùng với targetProduct
    // =========================================================
    @SuppressWarnings("unchecked")
    public List<RecommendationResponse> getFrequentlyBoughtTogether(
            UUID targetProductId,
            UUID storeId,
            int limit
    ) {
        String sql = """
            SELECT
                p.id                                    AS product_id,
                p.name                                  AS product_name,
                p.sku                                   AS product_sku,
                p.price                                 AS price,
                p.image_url                             AS image_url,
                p.stock_quantity                        AS stock_quantity,
                -- ⭐ Score = số lần mua cùng / tổng đơn có sản phẩm gốc
                --    Đây chính là chỉ số "Support" trong Market Basket Analysis
                COUNT(*)::FLOAT / (
                    SELECT COUNT(DISTINCT o2.id)
                    FROM orders o2
                    JOIN order_items oi3 ON o2.id = oi3.order_id
                    WHERE oi3.product_id = :targetProductId
                      AND o2.status IN ('CONFIRMED', 'COMPLETED')
                )                                       AS score
            FROM order_items oi1
            -- Self-join: tìm các sản phẩm xuất hiện trong CÙNG đơn hàng
            JOIN order_items oi2 ON oi1.order_id = oi2.order_id
                                AND oi2.product_id != :targetProductId
            JOIN orders o ON oi1.order_id = o.id
            JOIN products p ON oi2.product_id = p.id
            WHERE oi1.product_id = :targetProductId
              AND o.store_id     = :storeId
              AND o.status       IN ('CONFIRMED', 'COMPLETED')
              AND p.is_active    = true
              AND p.stock_quantity > 0
            GROUP BY p.id, p.name, p.sku, p.price, p.image_url, p.stock_quantity
            HAVING COUNT(*) >= 1     -- Tối thiểu mua cùng 1 lần
            ORDER BY score DESC
            LIMIT :limit
            """;

        return em.createNativeQuery(sql)
                .setParameter("targetProductId", targetProductId)
                .setParameter("storeId", storeId)
                .setParameter("limit", limit)
                .getResultList()
                .stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return RecommendationResponse.builder()
                            .productId(UUID.fromString(r[0].toString()))
                            .productName((String) r[1])
                            .productSku((String) r[2])
                            .price((BigDecimal) r[3])
                            .imageUrl((String) r[4])
                            .stockQuantity(((Number) r[5]).intValue())
                            .score(((Number) r[6]).doubleValue())
                            .reason("Thường được mua cùng nhau")
                            .build();
                })
                .toList();
    }

    // =========================================================
    // LOẠI 2: CUSTOMER HISTORY
    // Gợi ý dựa trên lịch sử mua của khách hàng cụ thể
    // Logic: Tìm sản phẩm khách CHƯA mua nhưng phổ biến ở store
    // =========================================================
    @SuppressWarnings("unchecked")
    public List<RecommendationResponse> getRecommendationsByCustomerHistory(
            UUID customerId,
            UUID storeId,
            int limit
    ) {
        String sql = """
            SELECT
                p.id                        AS product_id,
                p.name                      AS product_name,
                p.sku                       AS product_sku,
                p.price                     AS price,
                p.image_url                 AS image_url,
                p.stock_quantity            AS stock_quantity,
                COUNT(oi.id)::FLOAT         AS score
            FROM products p
            JOIN order_items oi  ON p.id = oi.product_id
            JOIN orders o        ON oi.order_id = o.id
            WHERE o.store_id  = :storeId
              AND o.status    IN ('CONFIRMED', 'COMPLETED')
              AND p.is_active = true
              AND p.stock_quantity > 0
              -- ⭐ Loại trừ sản phẩm khách đã mua rồi
              AND p.id NOT IN (
                  SELECT DISTINCT oi2.product_id
                  FROM order_items oi2
                  JOIN orders o2 ON oi2.order_id = o2.id
                  WHERE o2.customer_id = :customerId
                    AND o2.status IN ('CONFIRMED', 'COMPLETED')
              )
            GROUP BY p.id, p.name, p.sku, p.price, p.image_url, p.stock_quantity
            ORDER BY score DESC
            LIMIT :limit
            """;

        return em.createNativeQuery(sql)
                .setParameter("customerId", customerId)
                .setParameter("storeId", storeId)
                .setParameter("limit", limit)
                .getResultList()
                .stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return RecommendationResponse.builder()
                            .productId(UUID.fromString(r[0].toString()))
                            .productName((String) r[1])
                            .productSku((String) r[2])
                            .price((BigDecimal) r[3])
                            .imageUrl((String) r[4])
                            .stockQuantity(((Number) r[5]).intValue())
                            .score(((Number) r[6]).doubleValue())
                            .reason("Được nhiều khách hàng yêu thích")
                            .build();
                })
                .toList();
    }

    // =========================================================
    // LOẠI 3: SIMILAR CUSTOMERS (Collaborative Filtering đơn giản)
    // "Khách hàng mua giống bạn cũng mua..."
    // Logic: Tìm khách có lịch sử mua tương tự → lấy sản phẩm họ mua
    // =========================================================
    @SuppressWarnings("unchecked")
    public List<RecommendationResponse> getSimilarCustomerRecommendations(
            UUID customerId,
            UUID storeId,
            int limit
    ) {
        String sql = """
            WITH
            -- Bước 1: Lấy danh sách sản phẩm khách hiện tại đã mua
            customer_products AS (
                SELECT DISTINCT oi.product_id
                FROM order_items oi
                JOIN orders o ON oi.order_id = o.id
                WHERE o.customer_id = :customerId
                  AND o.status IN ('CONFIRMED', 'COMPLETED')
            ),
            -- Bước 2: Tìm khách hàng "tương tự"
            --         = những người đã mua ít nhất 1 sản phẩm giống
            similar_customers AS (
                SELECT
                    o.customer_id,
                    COUNT(DISTINCT oi.product_id) AS common_products
                FROM orders o
                JOIN order_items oi ON o.id = oi.order_id
                WHERE o.customer_id  != :customerId
                  AND o.customer_id  IS NOT NULL
                  AND o.store_id     = :storeId
                  AND o.status       IN ('CONFIRMED', 'COMPLETED')
                  AND oi.product_id  IN (SELECT product_id FROM customer_products)
                GROUP BY o.customer_id
                ORDER BY common_products DESC
                LIMIT 20   -- Chỉ xét 20 khách hàng tương tự nhất
            )
            -- Bước 3: Lấy sản phẩm khách tương tự đã mua
            --         nhưng khách hiện tại CHƯA mua
            SELECT
                p.id                            AS product_id,
                p.name                          AS product_name,
                p.sku                           AS product_sku,
                p.price                         AS price,
                p.image_url                     AS image_url,
                p.stock_quantity                AS stock_quantity,
                COUNT(DISTINCT o.customer_id)::FLOAT AS score
            FROM orders o
            JOIN order_items oi  ON o.id = oi.order_id
            JOIN products p      ON oi.product_id = p.id
            WHERE o.customer_id IN (SELECT customer_id FROM similar_customers)
              AND o.status       IN ('CONFIRMED', 'COMPLETED')
              AND p.is_active    = true
              AND p.stock_quantity > 0
              AND p.id NOT IN (SELECT product_id FROM customer_products)
            GROUP BY p.id, p.name, p.sku, p.price, p.image_url, p.stock_quantity
            ORDER BY score DESC
            LIMIT :limit
            """;

        return em.createNativeQuery(sql)
                .setParameter("customerId", customerId)
                .setParameter("storeId", storeId)
                .setParameter("limit", limit)
                .getResultList()
                .stream()
                .map(row -> {
                    Object[] r = (Object[]) row;
                    return RecommendationResponse.builder()
                            .productId(UUID.fromString(r[0].toString()))
                            .productName((String) r[1])
                            .productSku((String) r[2])
                            .price((BigDecimal) r[3])
                            .imageUrl((String) r[4])
                            .stockQuantity(((Number) r[5]).intValue())
                            .score(((Number) r[6]).doubleValue())
                            .reason("Khách hàng tương tự cũng mua")
                            .build();
                })
                .toList();
    }
}