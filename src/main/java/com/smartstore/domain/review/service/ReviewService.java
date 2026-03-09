// ReviewService.java
package com.smartstore.domain.review.service;

import com.smartstore.common.exception.BusinessException;
import com.smartstore.common.exception.ResourceNotFoundException;
import com.smartstore.domain.ai.service.GeminiService;
import com.smartstore.domain.product.entity.Product;
import com.smartstore.domain.product.repository.ProductRepository;
import com.smartstore.domain.review.dto.*;
import com.smartstore.domain.review.entity.Review;
import com.smartstore.domain.review.repository.ReviewRepository;
import com.smartstore.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final GeminiService geminiService;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, User user) {
        // Kiểm tra đã review chưa
        if (reviewRepository.existsByProductIdAndUserId(
                request.getProductId(), user.getId())) {
            throw new BusinessException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created for product: {}", product.getName());

        return toResponse(saved);
    }

    // Lấy tổng quan reviews kèm AI summary
    public ProductReviewSummary getProductReviewSummary(UUID productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Page<Review> reviewPage = reviewRepository
                .findByProductIdOrderByCreatedAtDesc(productId, pageable);

        Double avgRating = reviewRepository.getAverageRating(productId);
        Long totalReviews = reviewRepository.countByProductId(productId);

        // ⭐ Gọi Gemini AI để tóm tắt reviews
        String aiSummary = null;
        if (totalReviews > 0) {
            List<String> comments = reviewRepository
                    .findRecentCommentsByProductId(productId);
            aiSummary = geminiService.summarizeReviews(product.getName(), comments);
        }

        List<ReviewResponse> reviews = reviewPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return ProductReviewSummary.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews)
                .aiSummary(aiSummary)
                .reviews(reviews)
                .build();
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getName())
                .userId(r.getUser().getId())
                .userName(r.getUser().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .aiSummary(r.getAiSummary())
                .isVerified(r.getIsVerified())
                .createdAt(r.getCreatedAt())
                .build();
    }
}