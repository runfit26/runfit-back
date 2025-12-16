package com.runfit.domain.crew.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.runfit.global.config.AuditConfig;
import com.runfit.global.config.QueryDslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QueryDslConfig.class, AuditConfig.class})
class MembershipRepositoryTest {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CrewRepository crewRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private Crew crew;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.create("leader@test.com", "password", "리더"));
        user2 = userRepository.save(User.create("staff@test.com", "password", "스태프"));
        user3 = userRepository.save(User.create("member@test.com", "password", "멤버"));
        crew = crewRepository.save(Crew.create("테스트 크루", "설명", "서울", null));
    }

    @Test
    @DisplayName("멤버십 저장 및 조회 성공")
    void save_and_find_success() {
        // given
        Membership membership = Membership.createLeader(user1, crew);

        // when
        Membership saved = membershipRepository.save(membership);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole()).isEqualTo(CrewRole.LEADER);
    }

    @Test
    @DisplayName("사용자 ID와 크루 ID로 멤버십 존재 확인")
    void existsByUserUserIdAndCrewId_success() {
        // given
        membershipRepository.save(Membership.createLeader(user1, crew));

        // when
        boolean exists = membershipRepository.existsByUserUserIdAndCrewId(user1.getUserId(), crew.getId());
        boolean notExists = membershipRepository.existsByUserUserIdAndCrewId(user2.getUserId(), crew.getId());

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사용자 ID와 크루 ID로 멤버십 조회")
    void findByUserUserIdAndCrewId_success() {
        // given
        membershipRepository.save(Membership.createLeader(user1, crew));

        // when
        Optional<Membership> found = membershipRepository.findByUserUserIdAndCrewId(user1.getUserId(), crew.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUserId()).isEqualTo(user1.getUserId());
    }

    @Test
    @DisplayName("크루 ID로 모든 멤버십 조회 (User fetch join)")
    void findAllByCrewIdWithUser_success() {
        // given
        membershipRepository.save(Membership.createLeader(user1, crew));
        membershipRepository.save(Membership.createMember(user2, crew));
        membershipRepository.save(Membership.createMember(user3, crew));

        // when
        List<Membership> memberships = membershipRepository.findAllByCrewIdWithUser(crew.getId());

        // then
        assertThat(memberships).hasSize(3);
    }

    @Test
    @DisplayName("크루 ID와 역할로 멤버십 조회")
    void findAllByCrewIdAndRoleWithUser_success() {
        // given
        Membership leaderMembership = membershipRepository.save(Membership.createLeader(user1, crew));
        Membership memberMembership = membershipRepository.save(Membership.createMember(user2, crew));
        memberMembership.changeRole(CrewRole.STAFF);
        membershipRepository.save(memberMembership);
        membershipRepository.save(Membership.createMember(user3, crew));

        // when
        List<Membership> leaders = membershipRepository.findAllByCrewIdAndRoleWithUser(crew.getId(), CrewRole.LEADER);
        List<Membership> staffs = membershipRepository.findAllByCrewIdAndRoleWithUser(crew.getId(), CrewRole.STAFF);
        List<Membership> members = membershipRepository.findAllByCrewIdAndRoleWithUser(crew.getId(), CrewRole.MEMBER);

        // then
        assertThat(leaders).hasSize(1);
        assertThat(staffs).hasSize(1);
        assertThat(members).hasSize(1);
    }

    @Test
    @DisplayName("크루 ID와 역할로 멤버십 카운트")
    void countByCrewIdAndRole_success() {
        // given
        membershipRepository.save(Membership.createLeader(user1, crew));
        membershipRepository.save(Membership.createMember(user2, crew));
        membershipRepository.save(Membership.createMember(user3, crew));

        // when
        long leaderCount = membershipRepository.countByCrewIdAndRole(crew.getId(), CrewRole.LEADER);
        long memberCount = membershipRepository.countByCrewIdAndRole(crew.getId(), CrewRole.MEMBER);

        // then
        assertThat(leaderCount).isEqualTo(1);
        assertThat(memberCount).isEqualTo(2);
    }

    @Test
    @DisplayName("크루 ID로 전체 멤버 카운트")
    void countByCrewId_success() {
        // given
        membershipRepository.save(Membership.createLeader(user1, crew));
        membershipRepository.save(Membership.createMember(user2, crew));
        membershipRepository.save(Membership.createMember(user3, crew));

        // when
        long count = membershipRepository.countByCrewId(crew.getId());

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("멤버십이 없는 경우 빈 Optional 반환")
    void findByUserUserIdAndCrewId_notExists() {
        // when
        Optional<Membership> found = membershipRepository.findByUserUserIdAndCrewId(999L, crew.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("크루 ID로 모든 멤버십 조회 - 역할순 정렬")
    void findAllByCrewIdWithUserOrderByRole_success() {
        // given
        Membership memberMembership = membershipRepository.save(Membership.createMember(user3, crew));
        Membership staffMembership = membershipRepository.save(Membership.createMember(user2, crew));
        staffMembership.changeRole(CrewRole.STAFF);
        membershipRepository.save(staffMembership);
        Membership leaderMembership = membershipRepository.save(Membership.createLeader(user1, crew));

        // when
        List<Membership> memberships = membershipRepository.findAllByCrewIdWithUserOrderByRole(crew.getId());

        // then
        assertThat(memberships).hasSize(3);
        assertThat(memberships.get(0).getRole()).isEqualTo(CrewRole.LEADER);
        assertThat(memberships.get(1).getRole()).isEqualTo(CrewRole.STAFF);
        assertThat(memberships.get(2).getRole()).isEqualTo(CrewRole.MEMBER);
    }

    @Test
    @DisplayName("크루 ID로 모든 멤버십 조회 - 최근 가입순 정렬")
    void findAllByCrewIdWithUser_sortByJoinedAtDesc() {
        // given
        Membership first = membershipRepository.save(Membership.createLeader(user1, crew));
        Membership second = membershipRepository.save(Membership.createMember(user2, crew));
        Membership third = membershipRepository.save(Membership.createMember(user3, crew));

        // when
        List<Membership> memberships = membershipRepository.findAllByCrewIdWithUser(crew.getId());

        // then
        assertThat(memberships).hasSize(3);
        // 최근 가입순(DESC)이므로 마지막으로 가입한 사람이 첫 번째
        assertThat(memberships.get(0).getUser().getUserId()).isEqualTo(user3.getUserId());
        assertThat(memberships.get(1).getUser().getUserId()).isEqualTo(user2.getUserId());
        assertThat(memberships.get(2).getUser().getUserId()).isEqualTo(user1.getUserId());
    }
}
