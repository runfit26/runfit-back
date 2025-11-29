package com.runfit.global.jwt;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.auth.service.BlacklistTokenService;
import com.runfit.global.jwt.provider.AccessTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final AccessTokenProvider accessTokenProvider;
    private final BlacklistTokenService blacklistTokenService;

    public void validateAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new BusinessException(ErrorCode.ACCESS_TOKEN_NOT_FOUND);
        }

        if (blacklistTokenService.isAccessTokenBlacklisted(accessToken)) {
            throw new BusinessException(ErrorCode.ACCESS_TOKEN_BLACKLISTED);
        }

        String tokenType = accessTokenProvider.getTokenType(accessToken);
        if (!"access".equals(tokenType) || !accessTokenProvider.validateToken(accessToken)) {
            throw new BusinessException(ErrorCode.ACCESS_TOKEN_INVALID);
        }
    }
}
