package com.runfit.global.scheduler;

import com.runfit.domain.session.entity.SessionStatus;
import com.runfit.domain.session.repository.SessionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionStatusScheduler {

    private final SessionRepository sessionRepository;

    @Scheduled(cron = "0 * * * * *")  // 매분 0초에 실행
    @Transactional
    public void closeExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        int updatedCount = sessionRepository.updateStatusForExpiredRegistration(
            SessionStatus.OPEN,
            SessionStatus.CLOSED,
            now
        );

        if (updatedCount > 0) {
            log.info("Closed {} sessions with expired registration deadline", updatedCount);
        }
    }
}
