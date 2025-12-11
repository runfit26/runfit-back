package com.runfit.domain.auth.controller.dto.response;

public record TokenResponse(
    String token
) {

    public static TokenResponse from(String token) {
        return new TokenResponse(token);
    }
}
