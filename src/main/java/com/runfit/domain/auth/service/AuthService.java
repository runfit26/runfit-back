package com.runfit.domain.auth.service;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.auth.controller.dto.request.SignInRequest;
import com.runfit.domain.auth.controller.dto.request.SignUpRequest;
import com.runfit.domain.auth.controller.dto.response.SignUpResponse;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import com.runfit.global.jwt.RequestTokenExtractor;
import com.runfit.global.jwt.provider.AccessTokenProvider;
import com.runfit.global.jwt.provider.RefreshTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final BlacklistTokenService blacklistTokenService;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.create(request.email(), encodedPassword, request.name());
        User saved = userRepository.save(user);

        return SignUpResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public String[] signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!validatePassword(request.password(), user.getPassword())) {
            log.warn("올바르지 않은 정보로 로그인을 시도했습니다.: {}", request.email());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = accessTokenProvider.generateToken(user.getUserId(), user.getName());
        String refreshToken = refreshTokenProvider.generateToken(user.getUserId(), user.getName());

        return new String[]{accessToken, refreshToken};
    }

    private boolean validatePassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    public String[] refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!refreshTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String tokenType = refreshTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Claims claims = refreshTokenProvider.getClaims(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);

        String newAccessToken = accessTokenProvider.generateToken(userId, username);
        String newRefreshToken = refreshTokenProvider.generateToken(userId, username);

        return new String[]{newAccessToken, newRefreshToken};
    }

    public void signOut(HttpServletRequest request) {
        Optional<String> accessToken = RequestTokenExtractor.extractAccessToken(request);
        accessToken.ifPresent(blacklistTokenService::addAccessTokenToBlacklist);
    }

    public Long getRefreshTokenMaxAge() {
        return refreshTokenProvider.getRefreshExpiration() / 1000;
    }
}
