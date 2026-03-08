// TokenResponse.java
package com.smartstore.domain.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String tokenType;    // "Bearer"
    private long expiresIn;      // seconds
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private String id;
        private String email;
        private String fullName;
        private java.util.List<String> roles;
    }
}