package com.smartstore.domain.auth.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.auth.dto.*;
import com.smartstore.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(
            @Valid @RequestBody RegisterRequest request
            // ⭐ @Valid kích hoạt Bean Validation — kiểm tra các annotation
            //    @NotBlank, @Email... trong RegisterRequest
    ) {
        TokenResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}