// OrderRepository.java
package com.smartstore.domain.order.repository;

import com.smartstore.common.enums.OrderStatus;
import com.smartstore.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // ⭐ JOIN FETCH để load orderItems + products trong 1 query
    //    tránh N+1 query problem
    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.orderItems oi
        LEFT JOIN FETCH oi.product
        WHERE o.id = :id
        """)
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @Query("""
    SELECT o FROM Order o
    WHERE o.store.id = :storeId
      AND (:status IS NULL OR o.status = :status)
    ORDER BY o.createdAt DESC
    """)
    Page<Order> findByStoreWithFilters(
            @Param("storeId") UUID storeId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    // Lấy lịch sử đơn hàng của 1 khách hàng
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
}