// Review.java
package com.smartstore.domain.review.entity;

import com.smartstore.common.entity.BaseEntity;
import com.smartstore.domain.product.entity.Product;
import com.smartstore.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_product_id", columnList = "product_id"),
                @Index(name = "idx_reviews_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                // ⭐ 1 user chỉ review 1 lần cho mỗi sản phẩm
                @UniqueConstraint(
                        name = "uq_review_product_user",
                        columnNames = {"product_id", "user_id"}
                )
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;         // 1-5 sao

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;       // Tóm tắt từ Gemini AI

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;  // Đã mua sản phẩm chưa
}