package com.runfit.global.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.runfit.domain.session.entity.SessionStatus;
import com.runfit.domain.session.repository.SessionRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionStatusSchedulerTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionStatusScheduler sessionStatusScheduler;

    @Test
    @DisplayName("성공 - 스케줄러가 OPEN 세션을 CLOSED로 업데이트 호출")
    void closeExpiredSessions_callsRepository() {
        // given
        when(sessionRepository.updateStatusForExpiredRegistration(
            eq(SessionStatus.OPEN),
            eq(SessionStatus.CLOSED),
            any(LocalDateTime.class)
        )).thenReturn(5);

        // when
        sessionStatusScheduler.closeExpiredSessions();

        // then
        verify(sessionRepository).updateStatusForExpiredRegistration(
            eq(SessionStatus.OPEN),
            eq(SessionStatus.CLOSED),
            any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("성공 - 업데이트할 세션이 없어도 정상 동작")
    void closeExpiredSessions_noSessionsToUpdate() {
        // given
        when(sessionRepository.updateStatusForExpiredRegistration(
            eq(SessionStatus.OPEN),
            eq(SessionStatus.CLOSED),
            any(LocalDateTime.class)
        )).thenReturn(0);

        // when
        sessionStatusScheduler.closeExpiredSessions();

        // then
        verify(sessionRepository).updateStatusForExpiredRegistration(
            eq(SessionStatus.OPEN),
            eq(SessionStatus.CLOSED),
            any(LocalDateTime.class)
        );
    }
}
