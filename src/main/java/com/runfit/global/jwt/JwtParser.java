package com.runfit.global.jwt;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtParser {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_PREFIX_LENGTH = 7;

    public static Optional<String> extractAccessToken(String authorizationHeader) {
        return Optional.ofNullable(authorizationHeader)
            .filter(header -> header.startsWith(TOKEN_PREFIX))
            .map(header -> header.substring(TOKEN_PREFIX_LENGTH))
            .filter(token -> !token.isEmpty());
    }
}
