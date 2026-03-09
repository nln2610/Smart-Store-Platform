// ReviewController.java
package com.smartstore.domain.review.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.review.dto.*;
import com.smartstore.domain.review.service.ReviewService;
import com.smartstore.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        ReviewResponse response = reviewService.createReview(request, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ReviewResponse>success("Review created", response));
    }

    // ⭐ Endpoint này gọi Gemini AI để tóm tắt reviews
    @GetMapping("/products/{productId}/summary")
    public ResponseEntity<ApiResponse<ProductReviewSummary>> getProductSummary(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.<ProductReviewSummary>success(
                        reviewService.getProductReviewSummary(productId, pageable)));
    }
}