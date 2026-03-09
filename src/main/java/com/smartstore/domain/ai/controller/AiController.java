// AiController.java
package com.smartstore.domain.ai.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.ai.service.ChatbotService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestBody ChatRequest request
    ) {
        String reply = chatbotService.chat(request.getMessage(), request.getStoreId());
        return ResponseEntity.ok(
                ApiResponse.<ChatResponse>success(new ChatResponse(reply)));
    }

    @Data
    static class ChatRequest {
        @NotBlank
        private String message;
        private UUID storeId;
    }

    @Data
    static class ChatResponse {
        private final String reply;
    }
}