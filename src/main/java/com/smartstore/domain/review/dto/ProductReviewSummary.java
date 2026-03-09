// ProductReviewSummary.java
package com.smartstore.domain.review.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductReviewSummary {
    private Double averageRating;
    private Long totalReviews;
    private String aiSummary;        // Tóm tắt toàn bộ reviews bằng AI
    private List<ReviewResponse> reviews;
}