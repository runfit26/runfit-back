package com.runfit.global.jwt.provider;

import io.jsonwebtoken.Claims;

public interface JwtProvider {

    String generateToken(Long userId, String username, String role);

    boolean validateToken(String token);

    Claims getClaims(String token);

    String getTokenType(String token);
}
