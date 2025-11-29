package com.runfit.global.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runfit.common.exception.BusinessException;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.global.jwt.JwtValidator;
import com.runfit.global.jwt.RequestTokenExtractor;
import com.runfit.global.jwt.provider.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j(topic = "JwtAuthenticationFilter")
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider accessTokenProvider;
    private final JwtValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        Optional<String> accessToken = RequestTokenExtractor.extractAccessToken(request);

        if (accessToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = accessToken.get();

        try {
            jwtTokenValidator.validateAccessToken(token);
            Long userId = setAuthenticationUser(token, request);
            log.info("Authenticated user: {}", userId);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.");
            return;
        } catch (BusinessException e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            sendErrorResponse(response, e.getErrorCode().getStatus(), e.getErrorCode().getMessage());
            return;
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Long setAuthenticationUser(String accessToken, HttpServletRequest request) {
        Claims claims = accessTokenProvider.getClaims(accessToken);
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        AuthUser authUser = AuthUser.create(userId, username, role);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            authUser, null, authUser.getAuthorities()
        );

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return userId;
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
        throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorJson = new ObjectMapper().writeValueAsString(message);

        response.getWriter().write(errorJson);
    }

}