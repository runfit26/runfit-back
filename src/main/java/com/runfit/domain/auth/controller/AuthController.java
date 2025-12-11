package com.runfit.domain.auth.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.domain.auth.controller.dto.request.SignInRequest;
import com.runfit.domain.auth.controller.dto.request.SignUpRequest;
import com.runfit.domain.auth.controller.dto.response.SignOutResponse;
import com.runfit.domain.auth.controller.dto.response.SignUpResponse;
import com.runfit.domain.auth.controller.dto.response.TokenResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseWrapper<SignUpResponse>> signUp(
        @Valid @RequestBody SignUpRequest request
    ) {
        SignUpResponse response = authService.signUp(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseWrapper.success(response));
    }

    @PostMapping("/signin")
    public ResponseEntity<ResponseWrapper<TokenResponse>> signIn(
        @Valid @RequestBody SignInRequest request,
        HttpServletResponse response
    ) {
        String[] tokens = authService.signIn(request);
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        setRefreshTokenCookie(response, refreshToken, authService.getRefreshTokenMaxAge().intValue());

        return ResponseEntity
            .ok()
            .body(ResponseWrapper.success(TokenResponse.from(accessToken)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseWrapper<TokenResponse>> refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
        HttpServletResponse response
    ) {
        String[] tokens = authService.refresh(refreshToken);
        String newAccessToken = tokens[0];
        String newRefreshToken = tokens[1];

        setRefreshTokenCookie(response, newRefreshToken, authService.getRefreshTokenMaxAge().intValue());

        return ResponseEntity
            .ok()
            .body(ResponseWrapper.success(TokenResponse.from(newAccessToken)));
    }

    @PostMapping("/signout")
    public ResponseEntity<ResponseWrapper<SignOutResponse>> signOut(
        @AuthenticationPrincipal AuthUser user,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        authService.signOut(request);

        clearRefreshTokenCookie(response);

        return ResponseEntity
            .ok()
            .body(ResponseWrapper.success(SignOutResponse.of("로그아웃 되었습니다.")));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(REFRESH_TOKEN_COOKIE_PATH);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(REFRESH_TOKEN_COOKIE_PATH);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }
}
