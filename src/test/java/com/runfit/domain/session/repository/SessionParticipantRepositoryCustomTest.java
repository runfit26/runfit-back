package com.runfit.domain.session.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.crew.repository.CrewRepository;
import com.runfit.domain.crew.repository.MembershipRepository;
import com.runfit.domain.user.controller.dto.response.ParticipatingSessionResponse;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionParticipant;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import com.runfit.global.config.AuditConfig;
import com.runfit.global.config.QueryDslConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@DataJpaTest
@Import({QueryDslConfig.class, AuditConfig.class})
class SessionParticipantRepositoryCustomTest {

    @Autowired
    private SessionParticipantRepository sessionParticipantRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User hostUser;
    private Crew crew;

    @BeforeEach
    void setUp() {
        String uniqueId = String.valueOf(System.nanoTime());
        user1 = userRepository.save(User.create("user1-" + uniqueId + "@test.com", "password", "사용자1"));
        user2 = userRepository.save(User.create("user2-" + uniqueId + "@test.com", "password", "사용자2"));
        hostUser = userRepository.save(User.create("host-" + uniqueId + "@test.com", "password", "호스트"));

        crew = crewRepository.save(Crew.create("테스트 크루", "설명", "서울", null));
        membershipRepository.save(Membership.createLeader(hostUser, crew));
        membershipRepository.save(Membership.createMember(user1, crew));
        membershipRepository.save(Membership.createMember(user2, crew));
    }

    @Nested
    @DisplayName("내 참여 세션 목록 조회")
    class FindParticipatingSessionsByUserId {

        @Test
        @DisplayName("성공 - 호스트로 만든 세션 조회")
        void success_hostSessions() {
            // given
            Session session = sessionRepository.save(Session.create(
                crew, user1, "내가 만든 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("내가 만든 세션");
            assertThat(result.getContent().get(0).hostUserId()).isEqualTo(user1.getUserId());
        }

        @Test
        @DisplayName("성공 - 참여자로 참여한 세션 조회")
        void success_participantSessions() {
            // given
            Session session = sessionRepository.save(Session.create(
                crew, hostUser, "다른 사람이 만든 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));
            sessionParticipantRepository.save(SessionParticipant.create(session, user1));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("다른 사람이 만든 세션");
        }

        @Test
        @DisplayName("성공 - 호스트 + 참여자 모두 포함")
        void success_hostAndParticipantBoth() {
            // given
            // 내가 만든 세션
            Session mySession = sessionRepository.save(Session.create(
                crew, user1, "내가 만든 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            // 다른 사람이 만든 세션에 참여
            Session otherSession = sessionRepository.save(Session.create(
                crew, hostUser, "다른 사람 세션", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(4),
                SessionLevel.INTERMEDIATE, 360, 15
            ));
            sessionParticipantRepository.save(SessionParticipant.create(otherSession, user1));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(ParticipatingSessionResponse::name)
                .containsExactlyInAnyOrder("내가 만든 세션", "다른 사람 세션");
        }

        @Test
        @DisplayName("성공 - 호스트이면서 참여자인 경우 중복 없음")
        void success_hostAndParticipantNoDuplicate() {
            // given
            // 내가 만든 세션에 내가 참여자로도 등록
            Session session = sessionRepository.save(Session.create(
                crew, user1, "내가 만든 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));
            sessionParticipantRepository.save(SessionParticipant.create(session, user1));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1); // 중복 없이 1개만
            assertThat(result.getContent().get(0).name()).isEqualTo("내가 만든 세션");
        }

        @Test
        @DisplayName("성공 - SCHEDULED 필터 (예정된 세션만)")
        void success_scheduledFilter() {
            // given
            // 예정된 세션
            Session futureSession = sessionRepository.save(Session.create(
                crew, user1, "예정 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            // 완료된 세션
            Session pastSession = sessionRepository.save(Session.create(
                crew, user1, "완료 세션", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(8),
                SessionLevel.INTERMEDIATE, 360, 15
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), "SCHEDULED", PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("예정 세션");
        }

        @Test
        @DisplayName("성공 - COMPLETED 필터 (완료된 세션만)")
        void success_completedFilter() {
            // given
            // 예정된 세션
            Session futureSession = sessionRepository.save(Session.create(
                crew, user1, "예정 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            // 완료된 세션
            Session pastSession = sessionRepository.save(Session.create(
                crew, user1, "완료 세션", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(8),
                SessionLevel.INTERMEDIATE, 360, 15
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), "COMPLETED", PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("완료 세션");
        }

        @Test
        @DisplayName("성공 - 소문자 status도 동작")
        void success_lowercaseStatus() {
            // given
            Session futureSession = sessionRepository.save(Session.create(
                crew, user1, "예정 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), "scheduled", PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 삭제된 세션은 제외")
        void success_excludeDeletedSessions() {
            // given
            Session normalSession = sessionRepository.save(Session.create(
                crew, user1, "일반 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            Session deletedSession = sessionRepository.save(Session.create(
                crew, user1, "삭제된 세션", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(4),
                SessionLevel.INTERMEDIATE, 360, 15
            ));
            deletedSession.delete();
            sessionRepository.save(deletedSession);

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("일반 세션");
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void success_emptyResult() {
            // given - user1은 아무 세션에도 참여하지 않음

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - 페이지네이션")
        void success_pagination() {
            // given
            for (int i = 0; i < 5; i++) {
                sessionRepository.save(Session.create(
                    crew, user1, "세션" + i, "설명", null,
                    "서울", "강남구", null, 37.4979, 127.0276,
                    LocalDateTime.now().plusDays(i + 1),
                    LocalDateTime.now().plusDays(i),
                    SessionLevel.BEGINNER, 390, 20
                ));
            }

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 3)
            );

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("성공 - 다른 사용자의 세션은 조회되지 않음 (참여하지 않은 경우)")
        void success_otherUserSessionsNotIncluded() {
            // given
            Session user1Session = sessionRepository.save(Session.create(
                crew, user1, "user1 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            Session user2Session = sessionRepository.save(Session.create(
                crew, user2, "user2 세션", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(4),
                SessionLevel.INTERMEDIATE, 360, 15
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("user1 세션");
        }

        @Test
        @DisplayName("성공 - 참가자 카운트 정확성")
        void success_participantCountAccuracy() {
            // given
            Session session = sessionRepository.save(Session.create(
                crew, user1, "세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));
            sessionParticipantRepository.save(SessionParticipant.create(session, user1));
            sessionParticipantRepository.save(SessionParticipant.create(session, user2));
            sessionParticipantRepository.save(SessionParticipant.create(session, hostUser));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).currentParticipantCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("성공 - sessionAt 오름차순 정렬")
        void success_sortBySessionAtAsc() {
            // given
            Session session1 = sessionRepository.save(Session.create(
                crew, user1, "먼저 예정된 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(2),
                SessionLevel.BEGINNER, 390, 20
            ));

            Session session2 = sessionRepository.save(Session.create(
                crew, user1, "나중에 예정된 세션", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(9),
                SessionLevel.INTERMEDIATE, 360, 15
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            // sessionAt 오름차순이므로 먼저 예정된 세션이 먼저
            assertThat(result.getContent().get(0).name()).isEqualTo("먼저 예정된 세션");
            assertThat(result.getContent().get(1).name()).isEqualTo("나중에 예정된 세션");
        }

        @Test
        @DisplayName("성공 - 잘못된 status는 전체 조회와 동일")
        void success_invalidStatusReturnsAll() {
            // given
            Session futureSession = sessionRepository.save(Session.create(
                crew, user1, "예정 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            Session pastSession = sessionRepository.save(Session.create(
                crew, user1, "완료 세션", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(8),
                SessionLevel.INTERMEDIATE, 360, 15
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), "INVALID_STATUS", PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("성공 - participants 필드가 빈 리스트로 반환")
        void success_participantsEmpty() {
            // given
            Session session = sessionRepository.save(Session.create(
                crew, user1, "세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            // when
            Slice<ParticipatingSessionResponse> result = sessionParticipantRepository.findParticipatingSessionsByUserId(
                user1.getUserId(), null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).participants()).isNotNull();
            assertThat(result.getContent().get(0).participants()).isEmpty();
        }
    }

    @Nested
    @DisplayName("세션 ID 목록으로 참여자 조회")
    class FindParticipantsBySessionIds {

        @Test
        @DisplayName("성공 - 참여자 조회")
        void success() {
            // given
            Session session = sessionRepository.save(Session.create(
                crew, hostUser, "세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));
            sessionParticipantRepository.save(SessionParticipant.create(session, user1));
            sessionParticipantRepository.save(SessionParticipant.create(session, user2));

            // when
            List<SessionParticipant> result = sessionParticipantRepository.findParticipantsBySessionIds(
                List.of(session.getId())
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(sp -> sp.getUser().getUserId())
                .containsExactlyInAnyOrder(user1.getUserId(), user2.getUserId());
        }

        @Test
        @DisplayName("성공 - 여러 세션의 참여자 조회")
        void success_multipleSessions() {
            // given
            Session session1 = sessionRepository.save(Session.create(
                crew, hostUser, "세션1", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));
            Session session2 = sessionRepository.save(Session.create(
                crew, hostUser, "세션2", "설명", null,
                "서울", "송파구", null, 37.5145, 127.1017,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(4),
                SessionLevel.INTERMEDIATE, 360, 15
            ));

            sessionParticipantRepository.save(SessionParticipant.create(session1, user1));
            sessionParticipantRepository.save(SessionParticipant.create(session2, user2));

            // when
            List<SessionParticipant> result = sessionParticipantRepository.findParticipantsBySessionIds(
                List.of(session1.getId(), session2.getId())
            );

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 빈 세션 ID 목록")
        void success_emptySessionIds() {
            // when
            List<SessionParticipant> result = sessionParticipantRepository.findParticipantsBySessionIds(
                List.of()
            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - null 세션 ID 목록")
        void success_nullSessionIds() {
            // when
            List<SessionParticipant> result = sessionParticipantRepository.findParticipantsBySessionIds(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - joinedAt 내림차순 정렬")
        void success_sortByJoinedAtDesc() {
            // given
            Session session = sessionRepository.save(Session.create(
                crew, hostUser, "세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));

            // user1 먼저 참여, user2 나중에 참여
            SessionParticipant p1 = sessionParticipantRepository.save(SessionParticipant.create(session, user1));
            // 약간의 지연
            SessionParticipant p2 = sessionParticipantRepository.save(SessionParticipant.create(session, user2));

            // when
            List<SessionParticipant> result = sessionParticipantRepository.findParticipantsBySessionIds(
                List.of(session.getId())
            );

            // then
            assertThat(result).hasSize(2);
            // joinedAt 내림차순이므로 나중에 참여한 user2가 먼저
            assertThat(result.get(0).getUser().getUserId()).isEqualTo(user2.getUserId());
            assertThat(result.get(1).getUser().getUserId()).isEqualTo(user1.getUserId());
        }

        @Test
        @DisplayName("성공 - User fetch join 확인")
        void success_fetchJoinUser() {
            // given
            Session session = sessionRepository.save(Session.create(
                crew, hostUser, "세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 390, 20
            ));
            sessionParticipantRepository.save(SessionParticipant.create(session, user1));

            // when
            List<SessionParticipant> result = sessionParticipantRepository.findParticipantsBySessionIds(
                List.of(session.getId())
            );

            // then
            assertThat(result).hasSize(1);
            // User 정보가 fetch join되어 있어야 함
            assertThat(result.get(0).getUser().getName()).isEqualTo("사용자1");
        }
    }
}
