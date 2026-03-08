// RecommendationService.java
package com.smartstore.domain.recommendation.service;

import com.smartstore.domain.recommendation.dto.RecommendationResponse;
import com.smartstore.domain.recommendation.repository.RecommendationRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepositoryImpl recommendationRepository;

    // Gợi ý sản phẩm mua cùng
    public List<RecommendationResponse> getFrequentlyBoughtTogether(
            UUID productId, UUID storeId, int limit
    ) {
        return recommendationRepository
                .getFrequentlyBoughtTogether(productId, storeId, limit);
    }

    // Gợi ý cho khách hàng đã đăng nhập
    public List<RecommendationResponse> getPersonalizedRecommendations(
            UUID customerId, UUID storeId, int limit
    ) {
        // ⭐ Kết hợp cả 2 loại gợi ý, deduplicate, sort lại theo score
        List<RecommendationResponse> fromHistory =
                recommendationRepository.getRecommendationsByCustomerHistory(
                        customerId, storeId, limit);

        List<RecommendationResponse> fromSimilar =
                recommendationRepository.getSimilarCustomerRecommendations(
                        customerId, storeId, limit);

        // Merge và deduplicate theo productId
        Map<UUID, RecommendationResponse> merged = new java.util.LinkedHashMap<>();

        fromHistory.forEach(r -> merged.put(r.getProductId(), r));

        // Nếu sản phẩm đã có từ history, cộng thêm score từ similar
        fromSimilar.forEach(r -> merged.merge(
                r.getProductId(), r,
                (existing, newItem) -> {
                    existing.setScore(existing.getScore() + newItem.getScore());
                    return existing;
                }
        ));

        // Sort lại theo score tổng hợp
        return merged.values().stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}