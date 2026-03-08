// ProductRepository.java
package com.smartstore.domain.product.repository;

import com.smartstore.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(String sku);

    Optional<Product> findByIdAndIsActiveTrue(UUID id);

    // ⭐ JPQL query: tìm kiếm + lọc theo store, có phân trang
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.category
        WHERE p.store.id = :storeId
          AND p.isActive = true
          AND (:keyword IS NULL OR
               LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
        """)
    Page<Product> searchProducts(
            @Param("storeId") UUID storeId,
            @Param("keyword") String keyword,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

    // Tìm sản phẩm sắp hết hàng
    @Query("""
        SELECT p FROM Product p
        WHERE p.store.id = :storeId
          AND p.isActive = true
          AND p.stockQuantity <= p.minStockLevel
        ORDER BY p.stockQuantity ASC
        """)
    Page<Product> findLowStockProducts(@Param("storeId") UUID storeId, Pageable pageable);
}