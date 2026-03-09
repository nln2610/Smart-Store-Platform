// ReviewRepository.java
package com.smartstore.domain.review.repository;

import com.smartstore.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    Optional<Review> findByProductIdAndUserId(UUID productId, UUID userId);

    // Lấy tất cả comments để AI tóm tắt
    @Query("""
        SELECT r.comment FROM Review r
        WHERE r.product.id = :productId
          AND r.comment IS NOT NULL
          AND LENGTH(r.comment) > 0
        ORDER BY r.createdAt DESC
        LIMIT 20
        """)
    List<String> findRecentCommentsByProductId(@Param("productId") UUID productId);

    // Thống kê rating
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.product.id = :productId
        """)
    Double getAverageRating(@Param("productId") UUID productId);

    Long countByProductId(UUID productId);
}