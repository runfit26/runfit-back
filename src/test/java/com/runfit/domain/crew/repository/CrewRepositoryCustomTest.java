package com.runfit.domain.crew.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.controller.dto.request.CrewSearchCondition;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import com.runfit.global.config.AuditConfig;
import com.runfit.global.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@DataJpaTest
@Import({QueryDslConfig.class, AuditConfig.class})
class CrewRepositoryCustomTest {

    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        String uniqueId = String.valueOf(System.nanoTime());
        user = userRepository.save(User.create("custom-test-" + uniqueId + "@test.com", "password", "테스터"));

        // 크루 3개 생성
        Crew crew1 = crewRepository.save(Crew.create("서울 러닝 크루", "서울에서 달려요", "서울", null));
        Crew crew2 = crewRepository.save(Crew.create("부산 러닝 크루", "부산에서 달려요", "부산", null));
        Crew crew3 = crewRepository.save(Crew.create("서울 마라톤 크루", "마라톤 준비", "서울", null));

        // 멤버십 추가 (멤버 수 다르게)
        membershipRepository.save(Membership.createLeader(user, crew1));
        membershipRepository.save(Membership.createMember(user, crew2));
        membershipRepository.save(Membership.createLeader(user, crew3));

        // crew2에 멤버 추가로 더 넣기
        User user2 = userRepository.save(User.create("custom-test2-" + uniqueId + "@test.com", "password", "테스터2"));
        membershipRepository.save(Membership.createLeader(user2, crew2));
    }

    @Test
    @DisplayName("전체 크루 목록 조회 성공")
    void searchCrews_all_success() {
        // given
        CrewSearchCondition condition = CrewSearchCondition.of(null, null, null);

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("지역으로 필터링 성공")
    void searchCrews_byRegion_success() {
        // given
        CrewSearchCondition condition = CrewSearchCondition.of("서울", null, null);

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(crew -> crew.city().equals("서울"));
    }

    @Test
    @DisplayName("키워드로 검색 성공")
    void searchCrews_byKeyword_success() {
        // given
        CrewSearchCondition condition = CrewSearchCondition.of(null, "마라톤", null);

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).contains("마라톤");
    }

    @Test
    @DisplayName("지역 + 키워드 복합 검색 성공")
    void searchCrews_byRegionAndKeyword_success() {
        // given
        CrewSearchCondition condition = CrewSearchCondition.of("서울", "러닝", null);

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("서울 러닝 크루");
    }

    @Test
    @DisplayName("멤버 수로 정렬 성공")
    void searchCrews_sortByMemberCount_success() {
        // given
        CrewSearchCondition condition = CrewSearchCondition.of(null, null, "memberCountDesc");

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(3);
        // 멤버 수가 많은 순으로 정렬되었는지 확인
        assertThat(result.getContent().get(0).memberCount())
            .isGreaterThanOrEqualTo(result.getContent().get(1).memberCount());
    }

    @Test
    @DisplayName("페이지네이션 성공 - hasNext 확인")
    void searchCrews_pagination_hasNext() {
        // given
        CrewSearchCondition condition = CrewSearchCondition.of(null, null, null);

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 2));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("삭제된 크루는 조회되지 않음")
    void searchCrews_excludeDeleted() {
        // given
        Crew deletedCrew = crewRepository.save(Crew.create("삭제된 크루", "설명", "서울", null));
        deletedCrew.delete();
        crewRepository.save(deletedCrew);

        CrewSearchCondition condition = CrewSearchCondition.of(null, null, null);

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).noneMatch(crew -> crew.name().equals("삭제된 크루"));
    }

    @Test
    @DisplayName("검색 결과가 없을 때 빈 리스트 반환")
    void searchCrews_noResult() {
        // given
        CrewSearchCondition condition = CrewSearchCondition.of("제주", null, null);

        // when
        Slice<CrewListResponse> result = crewRepository.searchCrews(condition, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }
}
