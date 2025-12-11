package com.runfit.domain.auth.controller.dto.response;

public record SignOutResponse(
    String message
) {

    public static SignOutResponse of(String message) {
        return new SignOutResponse(message);
    }
}
