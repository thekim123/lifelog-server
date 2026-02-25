package com.younghwan.lifelog.auth;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record SignupRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {}

    public record UserResponse(Long id, String username, String role, String status) {
        public static UserResponse from(UserAccount user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getRole().name(), user.getStatus().name());
        }
    }
}
