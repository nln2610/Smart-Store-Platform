// ChatbotService.java
package com.smartstore.domain.ai.service;

import com.smartstore.domain.product.entity.Product;
import com.smartstore.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final GeminiService geminiService;
    private final ProductRepository productRepository;

    public String chat(String message, UUID storeId) {
        // ⭐ Lấy danh sách sản phẩm làm context cho AI
        //    RAG (Retrieval Augmented Generation) đơn giản
        String productContext = productRepository
                .searchProducts(storeId, null, null, PageRequest.of(0, 20))
                .getContent()
                .stream()
                .map(p -> "- %s: %,.0f VND (còn %d sản phẩm)"
                        .formatted(p.getName(), p.getPrice(), p.getStockQuantity()))
                .collect(Collectors.joining("\n"));

        return geminiService.chat(message, productContext);
    }
}