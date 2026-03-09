// ReviewResponse.java
package com.smartstore.domain.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String userName;
    private Integer rating;
    private String comment;
    private String aiSummary;
    private Boolean isVerified;
    private Instant createdAt;
}