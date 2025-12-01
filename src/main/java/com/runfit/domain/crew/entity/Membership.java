package com.runfit.domain.crew.entity;

import com.runfit.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@Table(
    name = "memberships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "crew_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private CrewRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    private Membership(User user, Crew crew, CrewRole role) {
        this.user = user;
        this.crew = crew;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public static Membership createLeader(User user, Crew crew) {
        return Membership.builder()
            .user(user)
            .crew(crew)
            .role(CrewRole.LEADER)
            .build();
    }

    public static Membership createMember(User user, Crew crew) {
        return Membership.builder()
            .user(user)
            .crew(crew)
            .role(CrewRole.MEMBER)
            .build();
    }

    public void changeRole(CrewRole newRole) {
        this.role = newRole;
    }

    public boolean isLeader() {
        return this.role == CrewRole.LEADER;
    }

    public boolean isStaff() {
        return this.role == CrewRole.STAFF;
    }

    public boolean isStaffOrHigher() {
        return this.role == CrewRole.LEADER || this.role == CrewRole.STAFF;
    }
}
