// RecommendationController.java
package com.smartstore.domain.recommendation.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.recommendation.dto.RecommendationResponse;
import com.smartstore.domain.recommendation.service.RecommendationService;
import com.smartstore.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    // Gợi ý sản phẩm mua cùng — hiển thị ở trang chi tiết sản phẩm
    @GetMapping("/products/{productId}/related")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRelated(
            @PathVariable UUID productId,
            @RequestParam UUID storeId,
            @RequestParam(defaultValue = "6") int limit
    ) {
        List<RecommendationResponse> result =
                recommendationService.getFrequentlyBoughtTogether(productId, storeId, limit);
        return ResponseEntity.ok(
                ApiResponse.<List<RecommendationResponse>>success(result));
    }

    // Gợi ý cá nhân hóa — hiển thị ở trang chủ hoặc trang khách hàng
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getForCustomer(
            @PathVariable UUID customerId,
            @RequestParam UUID storeId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<RecommendationResponse> result =
                recommendationService.getPersonalizedRecommendations(customerId, storeId, limit);
        return ResponseEntity.ok(
                ApiResponse.<List<RecommendationResponse>>success(result));
    }
}