package com.runfit.domain.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.crew.repository.CrewRepository;
import com.runfit.domain.crew.repository.MembershipRepository;
import com.runfit.domain.session.controller.dto.request.Coords;
import com.runfit.domain.session.controller.dto.request.SessionCreateRequest;
import com.runfit.domain.session.controller.dto.request.SessionUpdateRequest;
import com.runfit.domain.session.controller.dto.response.SessionDetailResponse;
import com.runfit.domain.session.controller.dto.response.SessionJoinResponse;
import com.runfit.domain.session.controller.dto.response.SessionLikeResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantsResponse;
import com.runfit.domain.session.controller.dto.response.SessionResponse;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionLike;
import com.runfit.domain.session.entity.SessionParticipant;
import com.runfit.domain.session.repository.SessionLikeRepository;
import com.runfit.domain.session.repository.SessionParticipantRepository;
import com.runfit.domain.session.repository.SessionRepository;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @InjectMocks
    private SessionService sessionService;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionParticipantRepository sessionParticipantRepository;

    @Mock
    private SessionLikeRepository sessionLikeRepository;

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    private User hostUser;
    private User participantUser;
    private Crew crew;
    private Membership staffMembership;
    private Membership memberMembership;
    private Session session;

    @BeforeEach
    void setUp() {
        hostUser = User.create("host@test.com", "password", "호스트");
        ReflectionTestUtils.setField(hostUser, "userId", 1L);

        participantUser = User.create("participant@test.com", "password", "참가자");
        ReflectionTestUtils.setField(participantUser, "userId", 2L);

        crew = Crew.create("테스트 크루", "설명", "서울", null);
        ReflectionTestUtils.setField(crew, "id", 1L);

        staffMembership = Membership.createLeader(hostUser, crew);
        ReflectionTestUtils.setField(staffMembership, "id", 1L);
        ReflectionTestUtils.setField(staffMembership, "role", CrewRole.STAFF);

        memberMembership = Membership.createMember(participantUser, crew);
        ReflectionTestUtils.setField(memberMembership, "id", 2L);

        session = Session.create(
            crew, hostUser, "테스트 세션", "설명", null,
            "서울", "송파구", null, 37.5145, 127.1017,
            LocalDateTime.now().plusDays(7),
            LocalDateTime.now().plusDays(6),
            SessionLevel.BEGINNER, 390, 20
        );
        ReflectionTestUtils.setField(session, "id", 1L);
    }

    @Nested
    @DisplayName("세션 생성")
    class CreateSession {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            SessionCreateRequest request = new SessionCreateRequest(
                1L, "한강 야간 러닝", "설명", null,
                "서울", "송파구", null, new Coords(37.5145, 127.1017),
                LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 20, 390
            );
            given(userRepository.findById(1L)).willReturn(Optional.of(hostUser));
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(staffMembership));
            given(sessionRepository.save(any(Session.class))).willAnswer(invocation -> {
                Session saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            // when
            SessionResponse response = sessionService.createSession(1L, request);

            // then
            assertThat(response.name()).isEqualTo("한강 야간 러닝");
            assertThat(response.crewId()).isEqualTo(1L);
            assertThat(response.hostUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 - 권한 없음 (일반 멤버)")
        void fail_noPermission() {
            // given
            SessionCreateRequest request = new SessionCreateRequest(
                1L, "세션", "설명", null,
                "서울", "송파구", null, new Coords(37.5145, 127.1017),
                LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 20, 390
            );
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when & then
            assertThatThrownBy(() -> sessionService.createSession(2L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_ROLE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 크루 없음")
        void fail_crewNotFound() {
            // given
            SessionCreateRequest request = new SessionCreateRequest(
                999L, "세션", "설명", null,
                "서울", "송파구", null, new Coords(37.5145, 127.1017),
                LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(6),
                SessionLevel.BEGINNER, 20, 390
            );
            given(userRepository.findById(1L)).willReturn(Optional.of(hostUser));
            given(crewRepository.findByIdAndDeletedIsNull(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.createSession(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("세션 상세 조회")
    class GetSessionDetail {

        @Test
        @DisplayName("성공 - 로그인 사용자")
        void success_withUser() {
            // given
            given(sessionRepository.findByIdWithCrewAndHostUser(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.countBySession(session)).willReturn(5L);
            given(sessionLikeRepository.existsBySessionIdAndUserUserId(1L, 2L)).willReturn(true);

            // when
            SessionDetailResponse response = sessionService.getSessionDetail(1L, 2L);

            // then
            assertThat(response.name()).isEqualTo("테스트 세션");
            assertThat(response.currentParticipantCount()).isEqualTo(5L);
            assertThat(response.liked()).isTrue();
        }

        @Test
        @DisplayName("성공 - 비로그인 사용자")
        void success_withoutUser() {
            // given
            given(sessionRepository.findByIdWithCrewAndHostUser(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.countBySession(session)).willReturn(5L);

            // when
            SessionDetailResponse response = sessionService.getSessionDetail(1L, null);

            // then
            assertThat(response.name()).isEqualTo("테스트 세션");
            assertThat(response.liked()).isFalse();
        }

        @Test
        @DisplayName("실패 - 세션 없음")
        void fail_sessionNotFound() {
            // given
            given(sessionRepository.findByIdWithCrewAndHostUser(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.getSessionDetail(999L, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("세션 참가 신청")
    class JoinSession {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.existsBySessionAndUser(session, participantUser)).willReturn(false);
            given(sessionParticipantRepository.countBySession(session)).willReturn(5L);
            given(sessionParticipantRepository.save(any(SessionParticipant.class))).willReturn(null);

            // when
            SessionJoinResponse response = sessionService.joinSession(2L, 1L);

            // then
            assertThat(response.message()).contains("참가 신청");
            assertThat(response.currentParticipantCount()).isEqualTo(6L);
            assertThat(response.maxParticipantCount()).isEqualTo(20);
        }

        @Test
        @DisplayName("실패 - 이미 참가")
        void fail_alreadyJoined() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.existsBySessionAndUser(session, participantUser)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sessionService.joinSession(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_JOINED_SESSION);
        }

        @Test
        @DisplayName("실패 - 정원 초과")
        void fail_sessionFull() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.existsBySessionAndUser(session, participantUser)).willReturn(false);
            given(sessionParticipantRepository.countBySession(session)).willReturn(20L);

            // when & then
            assertThatThrownBy(() -> sessionService.joinSession(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_FULL);
        }

        @Test
        @DisplayName("실패 - 세션 마감")
        void fail_sessionClosed() {
            // given
            session.close();
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));

            // when & then
            assertThatThrownBy(() -> sessionService.joinSession(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_CLOSED);
        }
    }

    @Nested
    @DisplayName("세션 참가 취소")
    class CancelJoinSession {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            SessionParticipant participant = SessionParticipant.create(session, participantUser);
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.findBySessionAndUser(session, participantUser))
                .willReturn(Optional.of(participant));
            given(sessionParticipantRepository.countBySession(session)).willReturn(4L);

            // when
            SessionJoinResponse response = sessionService.cancelJoinSession(2L, 1L);

            // then
            assertThat(response.message()).contains("취소");
            assertThat(response.currentParticipantCount()).isEqualTo(4L);
            verify(sessionParticipantRepository).delete(participant);
        }

        @Test
        @DisplayName("실패 - 참가하지 않은 세션")
        void fail_notParticipant() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.findBySessionAndUser(session, participantUser))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.cancelJoinSession(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_SESSION_PARTICIPANT);
        }
    }

    @Nested
    @DisplayName("세션 찜")
    class LikeSession {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionLikeRepository.existsBySessionAndUser(session, participantUser)).willReturn(false);
            given(sessionLikeRepository.save(any(SessionLike.class))).willReturn(null);

            // when
            SessionLikeResponse response = sessionService.likeSession(2L, 1L);

            // then
            assertThat(response.message()).contains("추가");
        }

        @Test
        @DisplayName("실패 - 이미 찜함")
        void fail_alreadyLiked() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionLikeRepository.existsBySessionAndUser(session, participantUser)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sessionService.likeSession(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_LIKED_SESSION);
        }
    }

    @Nested
    @DisplayName("세션 찜 취소")
    class UnlikeSession {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            SessionLike sessionLike = SessionLike.create(session, participantUser);
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionLikeRepository.findBySessionAndUser(session, participantUser))
                .willReturn(Optional.of(sessionLike));

            // when
            SessionLikeResponse response = sessionService.unlikeSession(2L, 1L);

            // then
            assertThat(response.message()).contains("취소");
            verify(sessionLikeRepository).delete(sessionLike);
        }

        @Test
        @DisplayName("실패 - 찜하지 않은 세션")
        void fail_notLiked() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(participantUser));
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionLikeRepository.findBySessionAndUser(session, participantUser))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.unlikeSession(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_LIKE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("세션 참가자 목록 조회")
    class GetSessionParticipants {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            SessionParticipant participant1 = SessionParticipant.create(session, hostUser);
            ReflectionTestUtils.setField(participant1, "joinedAt", LocalDateTime.now().minusDays(2));

            SessionParticipant participant2 = SessionParticipant.create(session, participantUser);
            ReflectionTestUtils.setField(participant2, "joinedAt", LocalDateTime.now().minusDays(1));

            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.findAllBySessionIdWithUser(1L))
                .willReturn(List.of(participant1, participant2));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L))
                .willReturn(Optional.of(staffMembership));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L))
                .willReturn(Optional.of(memberMembership));

            // when
            SessionParticipantsResponse response = sessionService.getSessionParticipants(1L);

            // then
            assertThat(response.totalCount()).isEqualTo(2);
            assertThat(response.participants()).hasSize(2);
            assertThat(response.participants().get(0).userId()).isEqualTo(1L);
            assertThat(response.participants().get(0).role()).isEqualTo(CrewRole.STAFF);
            assertThat(response.participants().get(1).userId()).isEqualTo(2L);
            assertThat(response.participants().get(1).role()).isEqualTo(CrewRole.MEMBER);
        }

        @Test
        @DisplayName("성공 - 참가자 없음")
        void success_noParticipants() {
            // given
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(sessionParticipantRepository.findAllBySessionIdWithUser(1L))
                .willReturn(List.of());

            // when
            SessionParticipantsResponse response = sessionService.getSessionParticipants(1L);

            // then
            assertThat(response.totalCount()).isEqualTo(0);
            assertThat(response.participants()).isEmpty();
        }

        @Test
        @DisplayName("실패 - 세션 없음")
        void fail_sessionNotFound() {
            // given
            given(sessionRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.getSessionParticipants(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("세션 정보 수정")
    class UpdateSession {

        private SessionUpdateRequest updateRequest;

        @BeforeEach
        void setUpUpdateRequest() {
            updateRequest = new SessionUpdateRequest(
                "수정된 세션명",
                "수정된 설명",
                "https://example.com/new-image.jpg"
            );
        }

        @Test
        @DisplayName("성공 - STAFF 권한")
        void success_asStaff() {
            // given
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(staffMembership));
            given(sessionParticipantRepository.countBySession(session)).willReturn(5L);

            // when
            SessionResponse response = sessionService.updateSession(1L, 1L, updateRequest);

            // then
            assertThat(response.name()).isEqualTo("수정된 세션명");
            assertThat(response.currentParticipantCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("성공 - LEADER 권한")
        void success_asLeader() {
            // given
            Membership leaderMembership = Membership.createLeader(hostUser, crew);
            ReflectionTestUtils.setField(leaderMembership, "id", 3L);

            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));
            given(sessionParticipantRepository.countBySession(session)).willReturn(3L);

            // when
            SessionResponse response = sessionService.updateSession(1L, 1L, updateRequest);

            // then
            assertThat(response.name()).isEqualTo("수정된 세션명");
            assertThat(response.currentParticipantCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("실패 - 권한 없음 (일반 멤버)")
        void fail_noPermission() {
            // given
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when & then
            assertThatThrownBy(() -> sessionService.updateSession(2L, 1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_ROLE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 세션 없음")
        void fail_sessionNotFound() {
            // given
            given(sessionRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.updateSession(1L, 999L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 멤버십 없음")
        void fail_membershipNotFound() {
            // given
            given(sessionRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(session));
            given(membershipRepository.findByUserUserIdAndCrewId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.updateSession(999L, 1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBERSHIP_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("세션 삭제")
    class DeleteSession {

        @Test
        @DisplayName("성공 - 세션 생성자가 삭제")
        void success() {
            // given
            given(sessionRepository.findByIdWithCrewAndHostUser(1L)).willReturn(Optional.of(session));

            // when
            sessionService.deleteSession(1L, 1L);

            // then
            assertThat(session.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("실패 - 세션 없음")
        void fail_sessionNotFound() {
            // given
            given(sessionRepository.findByIdWithCrewAndHostUser(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sessionService.deleteSession(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 권한 없음 (세션 생성자가 아님)")
        void fail_notHostUser() {
            // given
            given(sessionRepository.findByIdWithCrewAndHostUser(1L)).willReturn(Optional.of(session));

            // when & then
            assertThatThrownBy(() -> sessionService.deleteSession(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_DELETE_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("내가 만든 세션 목록 조회")
    class GetMyHostedSessions {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(sessionRepository.findMyHostedSessions(any(Long.class), any()))
                .willReturn(new org.springframework.data.domain.SliceImpl<>(List.of()));

            // when
            var result = sessionService.getMyHostedSessions(1L, org.springframework.data.domain.PageRequest.of(0, 10));

            // then
            assertThat(result).isNotNull();
            verify(sessionRepository).findMyHostedSessions(1L, org.springframework.data.domain.PageRequest.of(0, 10));
        }
    }
}
