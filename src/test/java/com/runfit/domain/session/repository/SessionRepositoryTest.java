package com.runfit.domain.session.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.crew.repository.CrewRepository;
import com.runfit.domain.crew.repository.MembershipRepository;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionStatus;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import com.runfit.global.config.AuditConfig;
import com.runfit.global.config.QueryDslConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QueryDslConfig.class, AuditConfig.class})
class SessionRepositoryTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User hostUser;
    private Crew crew;

    @BeforeEach
    void setUp() {
        String uniqueId = String.valueOf(System.nanoTime());
        hostUser = userRepository.save(User.create("host-" + uniqueId + "@test.com", "password", "호스트"));
        crew = crewRepository.save(Crew.create("테스트 크루", "설명", "서울", null));
        membershipRepository.save(Membership.createLeader(hostUser, crew));
    }

    @Nested
    @DisplayName("모집 마감 상태 일괄 업데이트")
    class UpdateStatusForExpiredRegistration {

        @Test
        @DisplayName("성공 - registerBy가 지난 OPEN 세션을 CLOSED로 변경")
        void updateExpiredSessions_success() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // registerBy가 이미 지난 세션 (OPEN)
            Session expiredSession = sessionRepository.save(Session.create(
                crew, hostUser, "마감된 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                now.plusDays(3),
                now.minusHours(1),  // registerBy가 1시간 전
                SessionLevel.BEGINNER, 390, 20
            ));

            assertThat(expiredSession.getStatus()).isEqualTo(SessionStatus.OPEN);

            // when
            int updatedCount = sessionRepository.updateStatusForExpiredRegistration(
                SessionStatus.OPEN,
                SessionStatus.CLOSED,
                now
            );

            // then
            assertThat(updatedCount).isEqualTo(1);

            Session updated = sessionRepository.findById(expiredSession.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(SessionStatus.CLOSED);
        }

        @Test
        @DisplayName("성공 - registerBy가 아직 안 지난 세션은 변경하지 않음")
        void notUpdateFutureSessions() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // registerBy가 아직 안 지난 세션
            Session futureSession = sessionRepository.save(Session.create(
                crew, hostUser, "모집중 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                now.plusDays(3),
                now.plusDays(1),  // registerBy가 내일
                SessionLevel.BEGINNER, 390, 20
            ));

            // when
            int updatedCount = sessionRepository.updateStatusForExpiredRegistration(
                SessionStatus.OPEN,
                SessionStatus.CLOSED,
                now
            );

            // then
            assertThat(updatedCount).isEqualTo(0);

            Session notUpdated = sessionRepository.findById(futureSession.getId()).orElseThrow();
            assertThat(notUpdated.getStatus()).isEqualTo(SessionStatus.OPEN);
        }

        @Test
        @DisplayName("성공 - 이미 CLOSED인 세션은 변경하지 않음")
        void notUpdateAlreadyClosedSessions() {
            // given
            LocalDateTime now = LocalDateTime.now();

            Session closedSession = sessionRepository.save(Session.create(
                crew, hostUser, "이미 마감된 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                now.plusDays(3),
                now.minusHours(1),
                SessionLevel.BEGINNER, 390, 20
            ));
            closedSession.close();  // 수동으로 마감
            sessionRepository.save(closedSession);

            // when
            int updatedCount = sessionRepository.updateStatusForExpiredRegistration(
                SessionStatus.OPEN,
                SessionStatus.CLOSED,
                now
            );

            // then
            assertThat(updatedCount).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - 삭제된 세션은 변경하지 않음")
        void notUpdateDeletedSessions() {
            // given
            LocalDateTime now = LocalDateTime.now();

            Session deletedSession = sessionRepository.save(Session.create(
                crew, hostUser, "삭제된 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                now.plusDays(3),
                now.minusHours(1),
                SessionLevel.BEGINNER, 390, 20
            ));
            deletedSession.delete();
            sessionRepository.save(deletedSession);

            // when
            int updatedCount = sessionRepository.updateStatusForExpiredRegistration(
                SessionStatus.OPEN,
                SessionStatus.CLOSED,
                now
            );

            // then
            assertThat(updatedCount).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - 여러 세션 동시 업데이트")
        void updateMultipleSessions() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // 마감된 세션 3개
            for (int i = 0; i < 3; i++) {
                sessionRepository.save(Session.create(
                    crew, hostUser, "마감된 세션 " + i, "설명", null,
                    "서울", "강남구", null, 37.4979, 127.0276,
                    now.plusDays(3),
                    now.minusHours(i + 1),  // 각각 1, 2, 3시간 전 마감
                    SessionLevel.BEGINNER, 390, 20
                ));
            }

            // 아직 모집중인 세션 2개
            for (int i = 0; i < 2; i++) {
                sessionRepository.save(Session.create(
                    crew, hostUser, "모집중 세션 " + i, "설명", null,
                    "서울", "강남구", null, 37.4979, 127.0276,
                    now.plusDays(3),
                    now.plusDays(i + 1),  // 내일, 모레 마감
                    SessionLevel.BEGINNER, 390, 20
                ));
            }

            // when
            int updatedCount = sessionRepository.updateStatusForExpiredRegistration(
                SessionStatus.OPEN,
                SessionStatus.CLOSED,
                now
            );

            // then
            assertThat(updatedCount).isEqualTo(3);
        }
    }
}
