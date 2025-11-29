package com.runfit.domain.auth.service;

import com.runfit.domain.auth.Entity.BlacklistToken;
import com.runfit.domain.auth.repository.BlacklistTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistTokenService {

    private final BlacklistTokenRepository blacklistTokenRepository;

    @Transactional
    public void addAccessTokenToBlacklist(String token) {
        log.info("Access token added to blacklist: {}", token);

        BlacklistToken blacklistToken = BlacklistToken.create(token);

        blacklistTokenRepository.save(blacklistToken);
    }

    @Transactional(readOnly = true)
    public boolean isAccessTokenBlacklisted(String token) {
        return blacklistTokenRepository.existsByToken(token);
    }
}