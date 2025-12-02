package com.runfit.domain.crew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.crew.controller.dto.request.CrewCreateRequest;
import com.runfit.domain.crew.controller.dto.request.CrewUpdateRequest;
import com.runfit.domain.crew.controller.dto.request.LeaderChangeRequest;
import com.runfit.domain.crew.controller.dto.request.RoleChangeRequest;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.crew.controller.dto.response.CrewMembersResponse;
import com.runfit.domain.crew.controller.dto.response.CrewResponse;
import com.runfit.domain.crew.controller.dto.response.LeaderChangeResponse;
import com.runfit.domain.crew.controller.dto.response.MemberCountResponse;
import com.runfit.domain.crew.controller.dto.response.MemberRoleResponse;
import com.runfit.domain.crew.controller.dto.response.MembershipResponse;
import com.runfit.domain.crew.controller.dto.response.RoleChangeResponse;
import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.crew.repository.CrewRepository;
import com.runfit.domain.crew.repository.MembershipRepository;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
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
class CrewServiceTest {

    @InjectMocks
    private CrewService crewService;

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private User anotherUser;
    private Crew crew;
    private Membership leaderMembership;
    private Membership memberMembership;

    @BeforeEach
    void setUp() {
        user = User.create("test@test.com", "password", "테스터");
        ReflectionTestUtils.setField(user, "userId", 1L);

        anotherUser = User.create("another@test.com", "password", "다른사용자");
        ReflectionTestUtils.setField(anotherUser, "userId", 2L);

        crew = Crew.create("테스트 크루", "설명", "서울", null);
        ReflectionTestUtils.setField(crew, "id", 1L);

        leaderMembership = Membership.createLeader(user, crew);
        ReflectionTestUtils.setField(leaderMembership, "id", 1L);

        memberMembership = Membership.createMember(anotherUser, crew);
        ReflectionTestUtils.setField(memberMembership, "id", 2L);
    }

    @Nested
    @DisplayName("크루 생성")
    class CreateCrew {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            CrewCreateRequest request = new CrewCreateRequest("새 크루", "설명", "서울", null);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(crewRepository.save(any(Crew.class))).willAnswer(invocation -> {
                Crew savedCrew = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedCrew, "id", 1L);
                return savedCrew;
            });
            given(membershipRepository.save(any(Membership.class))).willReturn(leaderMembership);

            // when
            CrewListResponse response = crewService.createCrew(1L, request);

            // then
            assertThat(response.name()).isEqualTo("새 크루");
            assertThat(response.memberCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void fail_userNotFound() {
            // given
            CrewCreateRequest request = new CrewCreateRequest("새 크루", "설명", "서울", null);
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.createCrew(999L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("크루 상세 조회")
    class GetCrewDetail {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));

            // when
            CrewResponse response = crewService.getCrewDetail(1L);

            // then
            assertThat(response.name()).isEqualTo("테스트 크루");
        }

        @Test
        @DisplayName("실패 - 크루 없음")
        void fail_crewNotFound() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.getCrewDetail(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("크루 정보 수정")
    class UpdateCrew {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            CrewUpdateRequest request = new CrewUpdateRequest("수정된 이름", "수정된 설명", "부산", null);
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));

            // when
            CrewResponse response = crewService.updateCrew(1L, 1L, request);

            // then
            assertThat(response.name()).isEqualTo("수정된 이름");
            assertThat(response.city()).isEqualTo("부산");
        }

        @Test
        @DisplayName("실패 - 리더가 아닌 사용자")
        void fail_notLeader() {
            // given
            CrewUpdateRequest request = new CrewUpdateRequest("수정된 이름", "설명", "서울", null);
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when & then
            assertThatThrownBy(() -> crewService.updateCrew(2L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_ROLE_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("크루 멤버 조회")
    class GetCrewMembers {

        @Test
        @DisplayName("전체 멤버 조회 성공")
        void success_all() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findAllByCrewIdWithUser(1L))
                .willReturn(List.of(leaderMembership, memberMembership));

            // when
            CrewMembersResponse response = crewService.getCrewMembers(1L, null);

            // then
            assertThat(response.leader()).isNotNull();
            assertThat(response.members()).hasSize(1);
        }

        @Test
        @DisplayName("역할별 필터링 조회 성공")
        void success_byRole() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findAllByCrewIdAndRoleWithUser(1L, CrewRole.LEADER))
                .willReturn(List.of(leaderMembership));

            // when
            CrewMembersResponse response = crewService.getCrewMembers(1L, "leader");

            // then
            assertThat(response.leader()).isNotNull();
            assertThat(response.staff()).isEmpty();
            assertThat(response.members()).isEmpty();
        }
    }

    @Nested
    @DisplayName("멤버 카운트 조회")
    class GetMemberCount {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.countByCrewIdAndRole(1L, CrewRole.LEADER)).willReturn(1L);
            given(membershipRepository.countByCrewIdAndRole(1L, CrewRole.STAFF)).willReturn(2L);
            given(membershipRepository.countByCrewIdAndRole(1L, CrewRole.MEMBER)).willReturn(10L);

            // when
            MemberCountResponse response = crewService.getMemberCount(1L);

            // then
            assertThat(response.leaderCount()).isEqualTo(1);
            assertThat(response.staffCount()).isEqualTo(2);
            assertThat(response.memberCount()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("크루장 변경")
    class ChangeLeader {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            LeaderChangeRequest request = new LeaderChangeRequest(2L);
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when
            LeaderChangeResponse response = crewService.changeLeader(1L, 1L, request);

            // then
            assertThat(response.oldLeaderId()).isEqualTo(1L);
            assertThat(response.newLeaderId()).isEqualTo(2L);
            assertThat(leaderMembership.getRole()).isEqualTo(CrewRole.MEMBER);
            assertThat(memberMembership.getRole()).isEqualTo(CrewRole.LEADER);
        }

        @Test
        @DisplayName("실패 - 대상 멤버십 없음")
        void fail_membershipNotFound() {
            // given
            LeaderChangeRequest request = new LeaderChangeRequest(999L);
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));
            given(membershipRepository.findByUserUserIdAndCrewId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.changeLeader(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBERSHIP_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("역할 변경")
    class ChangeRole {

        @Test
        @DisplayName("성공 - 멤버를 스태프로")
        void success_memberToStaff() {
            // given
            RoleChangeRequest request = new RoleChangeRequest(CrewRole.STAFF);
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when
            RoleChangeResponse response = crewService.changeRole(1L, 1L, 2L, request);

            // then
            assertThat(response.newRole()).isEqualTo(CrewRole.STAFF);
            assertThat(memberMembership.getRole()).isEqualTo(CrewRole.STAFF);
        }

        @Test
        @DisplayName("실패 - 리더 역할 변경 시도")
        void fail_cannotChangeLeaderRole() {
            // given
            RoleChangeRequest request = new RoleChangeRequest(CrewRole.STAFF);
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));

            // when & then
            assertThatThrownBy(() -> crewService.changeRole(1L, 1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_ROLE_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("크루 가입")
    class JoinCrew {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(userRepository.findById(2L)).willReturn(Optional.of(anotherUser));
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.existsByUserUserIdAndCrewId(2L, 1L)).willReturn(false);
            given(membershipRepository.save(any(Membership.class))).willReturn(memberMembership);

            // when
            MembershipResponse response = crewService.joinCrew(2L, 1L);

            // then
            assertThat(response.crewId()).isEqualTo(1L);
            assertThat(response.role()).isEqualTo(CrewRole.MEMBER);
        }

        @Test
        @DisplayName("실패 - 이미 가입된 크루")
        void fail_alreadyJoined() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.existsByUserUserIdAndCrewId(1L, 1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> crewService.joinCrew(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBERSHIP_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("크루 탈퇴")
    class LeaveCrew {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when
            crewService.leaveCrew(2L, 1L);

            // then
            verify(membershipRepository).delete(memberMembership);
        }

        @Test
        @DisplayName("실패 - 리더는 탈퇴 불가")
        void fail_leaderCannotLeave() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));

            // when & then
            assertThatThrownBy(() -> crewService.leaveCrew(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LEADER_CANNOT_LEAVE);
        }
    }

    @Nested
    @DisplayName("특정 사용자 역할 조회")
    class GetMemberRole {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when
            MemberRoleResponse response = crewService.getMemberRole(1L, 2L);

            // then
            assertThat(response.userId()).isEqualTo(2L);
            assertThat(response.role()).isEqualTo(CrewRole.MEMBER);
        }

        @Test
        @DisplayName("실패 - 크루 없음")
        void fail_crewNotFound() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.getMemberRole(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 멤버십 없음")
        void fail_membershipNotFound() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.getMemberRole(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBERSHIP_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("크루 멤버 강퇴")
    class KickMember {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when
            crewService.kickMember(1L, 1L, 2L);

            // then
            verify(membershipRepository).delete(memberMembership);
        }

        @Test
        @DisplayName("실패 - 리더가 아닌 사용자")
        void fail_notLeader() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when & then
            assertThatThrownBy(() -> crewService.kickMember(2L, 1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_ROLE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 리더 강퇴 시도")
        void fail_cannotKickLeader() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));

            // when & then
            assertThatThrownBy(() -> crewService.kickMember(1L, 1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_ROLE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 대상 멤버십 없음")
        void fail_targetNotFound() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));
            given(membershipRepository.findByUserUserIdAndCrewId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.kickMember(1L, 1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBERSHIP_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("크루 삭제")
    class DeleteCrew {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(1L, 1L)).willReturn(Optional.of(leaderMembership));

            // when
            crewService.deleteCrew(1L, 1L);

            // then
            assertThat(crew.getDeleted()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 리더가 아닌 사용자")
        void fail_notLeader() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(1L)).willReturn(Optional.of(crew));
            given(membershipRepository.findByUserUserIdAndCrewId(2L, 1L)).willReturn(Optional.of(memberMembership));

            // when & then
            assertThatThrownBy(() -> crewService.deleteCrew(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_ROLE_FORBIDDEN);
        }

        @Test
        @DisplayName("실패 - 크루 없음")
        void fail_crewNotFound() {
            // given
            given(crewRepository.findByIdAndDeletedIsNull(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> crewService.deleteCrew(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CREW_NOT_FOUND);
        }
    }
}
