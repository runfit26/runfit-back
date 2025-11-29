package com.runfit.global.jwt;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestTokenExtractor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static Optional<String> extractAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        return JwtParser.extractAccessToken(authorizationHeader);
    }
}
