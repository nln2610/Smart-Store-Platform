// GeminiService.java
package com.smartstore.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";

    public GeminiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // ⭐ WebClient là HTTP client non-blocking của Spring
        //    Thay thế cho RestTemplate (đã deprecated)
        this.webClient = WebClient.builder()
                .codecs(config -> config
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024)) // 2MB buffer
                .build();
    }

    // ===== TÓM TẮT REVIEWS =====
    public String summarizeReviews(String productName, List<String> comments) {
        if (comments == null || comments.isEmpty()) {
            return "Chưa có đánh giá nào cho sản phẩm này.";
        }

        // ⭐ Prompt engineering — cách viết prompt ảnh hưởng lớn đến chất lượng output
        String prompt = """
            Bạn là trợ lý phân tích đánh giá sản phẩm.
            Hãy tóm tắt các đánh giá sau về sản phẩm "%s" trong 2-3 câu ngắn gọn bằng tiếng Việt.
            Nêu rõ điểm tốt và điểm cần cải thiện (nếu có).
            Chỉ trả lời phần tóm tắt, không thêm tiêu đề hay giải thích.

            Các đánh giá:
            %s
            """.formatted(
                productName,
                String.join("\n- ", comments)
        );

        return callGeminiApi(prompt);
    }

    // ===== CHATBOT TƯ VẤN SẢN PHẨM =====
    public String chat(String userMessage, String context) {
        String prompt = """
            Bạn là trợ lý tư vấn bán hàng thân thiện của Smart Store.
            Chỉ tư vấn về các sản phẩm trong cửa hàng dựa trên thông tin được cung cấp.
            Trả lời ngắn gọn, thân thiện bằng tiếng Việt.

            Thông tin sản phẩm hiện có:
            %s

            Câu hỏi của khách: %s
            """.formatted(context, userMessage);

        return callGeminiApi(prompt);
    }

    // ===== PRIVATE: Gọi Gemini API =====
    private String callGeminiApi(String prompt) {
        try {
            // ⭐ Build request body theo format của Gemini API
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.7,      // 0=chính xác, 1=sáng tạo
                            "maxOutputTokens", 500   // Giới hạn độ dài response
                    )
            );

            String response = webClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // ⭐ block() chuyển từ async sang sync

            // Parse response JSON để lấy text
            JsonNode root = objectMapper.readTree(response);
            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText("Không thể tạo tóm tắt lúc này.");

        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage());
            // ⭐ Graceful degradation — lỗi AI không làm crash toàn bộ app
            return "Tính năng AI tạm thời không khả dụng.";
        }
    }
}