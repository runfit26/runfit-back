package com.runfit.domain.session.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.crew.repository.CrewRepository;
import com.runfit.domain.crew.repository.MembershipRepository;
import com.runfit.domain.session.controller.dto.request.SessionSearchCondition;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import com.runfit.global.config.AuditConfig;
import com.runfit.global.config.QueryDslConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
class SessionRepositoryCustomTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User hostUser;
    private Crew seoulCrew;
    private Crew gyeonggiCrew;

    @BeforeEach
    void setUp() {
        String uniqueId = String.valueOf(System.nanoTime());
        hostUser = userRepository.save(User.create("host-" + uniqueId + "@test.com", "password", "호스트"));

        seoulCrew = crewRepository.save(Crew.create("서울 러닝 크루", "서울 러닝", "서울", null));
        gyeonggiCrew = crewRepository.save(Crew.create("경기 러닝 크루", "경기 러닝", "경기", null));

        membershipRepository.save(Membership.createLeader(hostUser, seoulCrew));
        membershipRepository.save(Membership.createLeader(hostUser, gyeonggiCrew));

        LocalDateTime now = LocalDateTime.now();

        // 서울 강남구 세션 - 아침 시간 (08:00)
        sessionRepository.save(Session.create(
            seoulCrew, hostUser, "강남 아침 러닝", "설명", null,
            "서울", "강남구", null, 37.4979, 127.0276,
            now.plusDays(3).withHour(8).withMinute(0),
            now.plusDays(2),
            SessionLevel.BEGINNER, 390, 20
        ));

        // 서울 송파구 세션 - 저녁 시간 (20:00)
        sessionRepository.save(Session.create(
            seoulCrew, hostUser, "송파 저녁 러닝", "설명", null,
            "서울", "송파구", null, 37.5145, 127.1017,
            now.plusDays(5).withHour(20).withMinute(0),
            now.plusDays(4),
            SessionLevel.INTERMEDIATE, 360, 15
        ));

        // 경기 가평군 세션 - 점심 시간 (12:00)
        sessionRepository.save(Session.create(
            gyeonggiCrew, hostUser, "가평 트레일 러닝", "설명", null,
            "경기", "가평군", null, 37.8315, 127.5095,
            now.plusDays(7).withHour(12).withMinute(0),
            now.plusDays(6),
            SessionLevel.ADVANCED, 330, 10
        ));

        // 경기 성남시 세션 - 저녁 시간 (19:30)
        sessionRepository.save(Session.create(
            gyeonggiCrew, hostUser, "성남 야간 러닝", "설명", null,
            "경기", "성남시", null, 37.4200, 127.1267,
            now.plusDays(2).withHour(19).withMinute(30),
            now.plusDays(1),
            SessionLevel.BEGINNER, 420, 25
        ));
    }

    @Nested
    @DisplayName("세션 목록 조회")
    class SearchSessions {

        @Test
        @DisplayName("전체 세션 목록 조회 성공")
        void searchSessions_all_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, null, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(4);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("단일 도시 필터링 성공")
        void searchSessions_bySingleCity_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                List.of("서울"), null, null, null, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(s -> s.city().equals("서울"));
        }

        @Test
        @DisplayName("복수 도시 필터링 성공")
        void searchSessions_byMultipleCities_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                List.of("서울", "경기"), null, null, null, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(4);
        }

        @Test
        @DisplayName("시/군/구 필터링 성공")
        void searchSessions_byDistrict_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, List.of("강남구", "송파구"), null, null, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .allMatch(s -> s.district().equals("강남구") || s.district().equals("송파구"));
        }

        @Test
        @DisplayName("난이도 필터링 성공")
        void searchSessions_byLevel_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, SessionLevel.BEGINNER, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).allMatch(s -> s.level() == SessionLevel.BEGINNER);
        }

        @Test
        @DisplayName("시간대 필터링 성공 - 저녁 시간대 (sessionAt 기준)")
        void searchSessions_byTimeRange_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, null, null, null,
                LocalTime.of(19, 0), LocalTime.of(23, 0), null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .allMatch(s -> {
                    LocalTime sessionTime = s.sessionAt().toLocalTime();
                    return !sessionTime.isBefore(LocalTime.of(19, 0)) && !sessionTime.isAfter(LocalTime.of(23, 0));
                });
        }

        @Test
        @DisplayName("날짜 범위 필터링 성공 (sessionAt 기준)")
        void searchSessions_byDateRange_success() {
            // given
            LocalDate today = LocalDate.now();
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, null,
                today.plusDays(2), today.plusDays(4),
                null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getContent())
                .allMatch(s -> {
                    LocalDate sessionDate = s.sessionAt().toLocalDate();
                    return !sessionDate.isBefore(today.plusDays(2)) &&
                           !sessionDate.isAfter(today.plusDays(4));
                });
        }

        @Test
        @DisplayName("복합 필터링 성공 - 도시 + 난이도")
        void searchSessions_byCityAndLevel_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                List.of("서울"), null, null, SessionLevel.BEGINNER, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).city()).isEqualTo("서울");
            assertThat(result.getContent().get(0).level()).isEqualTo(SessionLevel.BEGINNER);
        }
    }

    @Nested
    @DisplayName("정렬")
    class SortSessions {

        @Test
        @DisplayName("최근 생성순 정렬 (기본)")
        void searchSessions_sortByCreatedAtDesc_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, null, null, null, null, null, "createdAtDesc"
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(4);
            // 최근 생성된 것이 먼저 (성남 야간 러닝이 마지막에 생성됨)
            assertThat(result.getContent().get(0).name()).isEqualTo("성남 야간 러닝");
        }

        @Test
        @DisplayName("모임 시작일순 정렬")
        void searchSessions_sortBySessionAtAsc_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, null, null, null, null, null, "sessionAtAsc"
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(4);
            // sessionAt이 가장 빠른 순서
            for (int i = 0; i < result.getContent().size() - 1; i++) {
                assertThat(result.getContent().get(i).sessionAt())
                    .isBeforeOrEqualTo(result.getContent().get(i + 1).sessionAt());
            }
        }

        @Test
        @DisplayName("마감 임박순 정렬")
        void searchSessions_sortByRegisterByAsc_success() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, null, null, null, null, null, "registerByAsc"
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(4);
            // registerBy가 가장 빠른 순서
            for (int i = 0; i < result.getContent().size() - 1; i++) {
                assertThat(result.getContent().get(i).registerBy())
                    .isBeforeOrEqualTo(result.getContent().get(i + 1).registerBy());
            }
        }
    }

    @Nested
    @DisplayName("페이지네이션")
    class Pagination {

        @Test
        @DisplayName("페이지네이션 - hasNext 확인")
        void searchSessions_pagination_hasNext() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                null, null, null, null, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 2)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("검색 결과 없을 때 빈 리스트 반환")
        void searchSessions_noResult() {
            // given
            SessionSearchCondition condition = SessionSearchCondition.of(
                List.of("부산"), null, null, null, null, null, null, null, null
            );

            // when
            Slice<SessionListResponse> result = sessionRepository.searchSessions(
                condition, null, PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("내가 만든 세션 목록 조회")
    class FindMyHostedSessions {

        @Test
        @DisplayName("성공 - 세션 목록 조회")
        void findMyHostedSessions_success() {
            // when
            Slice<SessionListResponse> result = sessionRepository.findMyHostedSessions(
                hostUser.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(4);
            assertThat(result.getContent()).allMatch(s -> s.hostUserId().equals(hostUser.getUserId()));
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - 페이지네이션")
        void findMyHostedSessions_pagination() {
            // when
            Slice<SessionListResponse> result = sessionRepository.findMyHostedSessions(
                hostUser.getUserId(), PageRequest.of(0, 2)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("성공 - 다른 사용자는 빈 리스트")
        void findMyHostedSessions_otherUser_empty() {
            // given
            String uniqueId = String.valueOf(System.nanoTime());
            User otherUser = userRepository.save(User.create("other-" + uniqueId + "@test.com", "password", "다른유저"));

            // when
            Slice<SessionListResponse> result = sessionRepository.findMyHostedSessions(
                otherUser.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - 삭제된 세션은 제외")
        void findMyHostedSessions_excludeDeleted() {
            // given
            Session deletedSession = sessionRepository.save(Session.create(
                seoulCrew, hostUser, "삭제될 세션", "설명", null,
                "서울", "강남구", null, 37.4979, 127.0276,
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(9),
                SessionLevel.BEGINNER, 390, 20
            ));
            deletedSession.delete();
            sessionRepository.save(deletedSession);

            // when
            Slice<SessionListResponse> result = sessionRepository.findMyHostedSessions(
                hostUser.getUserId(), PageRequest.of(0, 20)
            );

            // then
            assertThat(result.getContent()).hasSize(4); // 삭제된 세션 제외
            assertThat(result.getContent()).noneMatch(s -> s.name().equals("삭제될 세션"));
        }

        @Test
        @DisplayName("성공 - 최근 생성순 정렬")
        void findMyHostedSessions_sortByCreatedAtDesc() {
            // when
            Slice<SessionListResponse> result = sessionRepository.findMyHostedSessions(
                hostUser.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(4);
            // 마지막으로 생성된 "성남 야간 러닝"이 첫 번째로 나와야 함
            assertThat(result.getContent().get(0).name()).isEqualTo("성남 야간 러닝");
        }
    }
}
