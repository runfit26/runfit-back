package com.runfit.domain.crew.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MembershipTest {

    private User user;
    private Crew crew;

    @BeforeEach
    void setUp() {
        user = User.create("test@test.com", "password", "테스터");
        crew = Crew.create("테스트 크루", "설명", "서울", null);
    }

    @Test
    @DisplayName("리더 멤버십 생성 성공")
    void createLeader_success() {
        // when
        Membership membership = Membership.createLeader(user, crew);

        // then
        assertThat(membership.getUser()).isEqualTo(user);
        assertThat(membership.getCrew()).isEqualTo(crew);
        assertThat(membership.getRole()).isEqualTo(CrewRole.LEADER);
        assertThat(membership.isLeader()).isTrue();
        assertThat(membership.isStaff()).isFalse();
        assertThat(membership.getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("일반 멤버 멤버십 생성 성공")
    void createMember_success() {
        // when
        Membership membership = Membership.createMember(user, crew);

        // then
        assertThat(membership.getRole()).isEqualTo(CrewRole.MEMBER);
        assertThat(membership.isLeader()).isFalse();
        assertThat(membership.isStaff()).isFalse();
        assertThat(membership.isStaffOrHigher()).isFalse();
    }

    @Test
    @DisplayName("역할 변경 성공")
    void changeRole_success() {
        // given
        Membership membership = Membership.createMember(user, crew);
        assertThat(membership.getRole()).isEqualTo(CrewRole.MEMBER);

        // when
        membership.changeRole(CrewRole.STAFF);

        // then
        assertThat(membership.getRole()).isEqualTo(CrewRole.STAFF);
        assertThat(membership.isStaff()).isTrue();
        assertThat(membership.isStaffOrHigher()).isTrue();
    }

    @Test
    @DisplayName("리더는 isStaffOrHigher가 true")
    void leader_isStaffOrHigher_true() {
        // given
        Membership membership = Membership.createLeader(user, crew);

        // then
        assertThat(membership.isStaffOrHigher()).isTrue();
    }

    @Test
    @DisplayName("스태프는 isStaffOrHigher가 true")
    void staff_isStaffOrHigher_true() {
        // given
        Membership membership = Membership.createMember(user, crew);
        membership.changeRole(CrewRole.STAFF);

        // then
        assertThat(membership.isStaffOrHigher()).isTrue();
    }
}
