package com.runfit.domain.crew.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.user.controller.dto.response.MyCrewResponse;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import com.runfit.global.config.AuditConfig;
import com.runfit.global.config.QueryDslConfig;
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
class MembershipRepositoryCustomTest {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Crew crew1;
    private Crew crew2;
    private Crew crew3;
    private Crew deletedCrew;

    @BeforeEach
    void setUp() {
        String uniqueId = String.valueOf(System.nanoTime());
        user1 = userRepository.save(User.create("user1-" + uniqueId + "@test.com", "password", "사용자1"));
        user2 = userRepository.save(User.create("user2-" + uniqueId + "@test.com", "password", "사용자2"));

        crew1 = crewRepository.save(Crew.create("크루1", "설명1", "서울", null));
        crew2 = crewRepository.save(Crew.create("크루2", "설명2", "부산", null));
        crew3 = crewRepository.save(Crew.create("크루3", "설명3", "대구", null));
        deletedCrew = crewRepository.save(Crew.create("삭제된 크루", "설명", "인천", null));
        deletedCrew.delete();
        crewRepository.save(deletedCrew);
    }

    @Nested
    @DisplayName("내가 만든 크루 목록 조회")
    class FindOwnedCrewsByUserId {

        @Test
        @DisplayName("성공 - 리더인 크루만 조회")
        void success_onlyLeaderCrews() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createLeader(user1, crew2));
            membershipRepository.save(Membership.createMember(user1, crew3));

            // when
            Slice<CrewListResponse> result = membershipRepository.findOwnedCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .extracting(CrewListResponse::name)
                .containsExactlyInAnyOrder("크루1", "크루2");
        }

        @Test
        @DisplayName("성공 - 삭제된 크루는 제외")
        void success_excludeDeletedCrews() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createLeader(user1, deletedCrew));

            // when
            Slice<CrewListResponse> result = membershipRepository.findOwnedCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("크루1");
        }

        @Test
        @DisplayName("성공 - 멤버 카운트 정확성")
        void success_memberCountAccuracy() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createMember(user2, crew1));

            // when
            Slice<CrewListResponse> result = membershipRepository.findOwnedCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).memberCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void success_emptyResult() {
            // given - user1은 아무 크루도 만들지 않음

            // when
            Slice<CrewListResponse> result = membershipRepository.findOwnedCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - STAFF는 포함되지 않음")
        void success_staffNotIncluded() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            Membership staffMembership = Membership.createMember(user1, crew2);
            staffMembership.changeRole(CrewRole.STAFF);
            membershipRepository.save(staffMembership);

            // when
            Slice<CrewListResponse> result = membershipRepository.findOwnedCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("크루1");
        }

        @Test
        @DisplayName("성공 - 페이지네이션")
        void success_pagination() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createLeader(user1, crew2));
            membershipRepository.save(Membership.createLeader(user1, crew3));

            // when
            Slice<CrewListResponse> result = membershipRepository.findOwnedCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 2)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("성공 - 다른 사용자의 크루는 조회되지 않음")
        void success_otherUserCrewsNotIncluded() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createLeader(user2, crew2));

            // when
            Slice<CrewListResponse> result = membershipRepository.findOwnedCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("크루1");
        }
    }

    @Nested
    @DisplayName("내가 속한 크루 목록 조회")
    class FindMyCrewsByUserId {

        @Test
        @DisplayName("성공 - 모든 역할의 크루 조회")
        void success_allRoles() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            Membership staffMembership = Membership.createMember(user1, crew2);
            staffMembership.changeRole(CrewRole.STAFF);
            membershipRepository.save(staffMembership);
            membershipRepository.save(Membership.createMember(user1, crew3));

            // when
            Slice<MyCrewResponse> result = membershipRepository.findMyCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                .extracting(MyCrewResponse::myRole)
                .containsExactlyInAnyOrder(CrewRole.LEADER, CrewRole.STAFF, CrewRole.MEMBER);
        }

        @Test
        @DisplayName("성공 - myRole 필드 정확성")
        void success_myRoleAccuracy() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createMember(user1, crew2));

            // when
            Slice<MyCrewResponse> result = membershipRepository.findMyCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            MyCrewResponse leaderCrew = result.getContent().stream()
                .filter(c -> c.name().equals("크루1"))
                .findFirst().orElseThrow();
            MyCrewResponse memberCrew = result.getContent().stream()
                .filter(c -> c.name().equals("크루2"))
                .findFirst().orElseThrow();

            assertThat(leaderCrew.myRole()).isEqualTo(CrewRole.LEADER);
            assertThat(memberCrew.myRole()).isEqualTo(CrewRole.MEMBER);
        }

        @Test
        @DisplayName("성공 - 삭제된 크루는 제외")
        void success_excludeDeletedCrews() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createMember(user1, deletedCrew));

            // when
            Slice<MyCrewResponse> result = membershipRepository.findMyCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("크루1");
        }

        @Test
        @DisplayName("성공 - 멤버 카운트 정확성")
        void success_memberCountAccuracy() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createMember(user2, crew1));

            // when
            Slice<MyCrewResponse> result = membershipRepository.findMyCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).memberCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void success_emptyResult() {
            // given - user1은 아무 크루에도 속하지 않음

            // when
            Slice<MyCrewResponse> result = membershipRepository.findMyCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("성공 - 페이지네이션")
        void success_pagination() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createMember(user1, crew2));
            membershipRepository.save(Membership.createMember(user1, crew3));

            // when
            Slice<MyCrewResponse> result = membershipRepository.findMyCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 2)
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("성공 - 다른 사용자의 크루는 조회되지 않음")
        void success_otherUserCrewsNotIncluded() {
            // given
            membershipRepository.save(Membership.createLeader(user1, crew1));
            membershipRepository.save(Membership.createLeader(user2, crew2));

            // when
            Slice<MyCrewResponse> result = membershipRepository.findMyCrewsByUserId(
                user1.getUserId(), PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).name()).isEqualTo("크루1");
        }
    }
}
