package com.runfit.domain.auth.controller.dto.response;

public record SignInResponse(
    String token
) {

    public static SignInResponse from(String token) {
        return new SignInResponse(token);
    }
}
