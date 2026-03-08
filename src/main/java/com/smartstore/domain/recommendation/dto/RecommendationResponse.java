// RecommendationResponse.java
package com.smartstore.domain.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private UUID productId;
    private String productName;
    private String productSku;
    private BigDecimal price;
    private String imageUrl;
    private Integer stockQuantity;
    private Double score;           // Điểm gợi ý — càng cao càng liên quan
    private String reason;          // Lý do gợi ý — hiển thị cho user
}