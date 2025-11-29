package com.runfit.domain.auth.service;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.auth.controller.dto.request.SignInRequest;
import com.runfit.domain.auth.controller.dto.request.SignUpRequest;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import com.runfit.global.jwt.RequestTokenExtractor;
import com.runfit.global.jwt.provider.JwtProvider;
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
    private final JwtProvider accessTokenProvider;
    private final BlacklistTokenService blacklistTokenService;

    @Transactional
    public Long signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.create(request.email(), encodedPassword, request.name());
        User saved = userRepository.save(user);

        return saved.getUserId();
    }

    @Transactional(readOnly = true)
    public String signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!validatePassword(request.password(), user.getPassword())) {
            log.warn("올바르지 않은 정보로 로그인을 시도했습니다.: {}", request.email());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return accessTokenProvider.generateToken(user.getUserId(), user.getName());
    }

    private boolean validatePassword(String password, String password1) {
        return passwordEncoder.matches(password, password1);
    }

    public void signOut(HttpServletRequest request) {
        Optional<String> accessToken = RequestTokenExtractor.extractAccessToken(request);

        accessToken.ifPresent(blacklistTokenService::addAccessTokenToBlacklist);
    }
}
