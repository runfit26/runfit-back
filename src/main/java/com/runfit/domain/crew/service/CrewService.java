package com.runfit.domain.crew.service;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.crew.controller.dto.request.CrewCreateRequest;
import com.runfit.domain.crew.controller.dto.request.CrewSearchCondition;
import com.runfit.domain.crew.controller.dto.request.CrewUpdateRequest;
import com.runfit.domain.crew.controller.dto.request.LeaderChangeRequest;
import com.runfit.domain.crew.controller.dto.request.RoleChangeRequest;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.crew.controller.dto.response.CrewMembersResponse;
import com.runfit.domain.crew.controller.dto.response.CrewResponse;
import com.runfit.domain.crew.controller.dto.response.LeaderChangeResponse;
import com.runfit.domain.crew.controller.dto.response.MemberCountResponse;
import com.runfit.domain.crew.controller.dto.response.MemberResponse;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Transactional
    public CrewListResponse createCrew(Long userId, CrewCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Crew crew = Crew.create(
            request.name(),
            request.description(),
            request.region(),
            request.image()
        );
        Crew savedCrew = crewRepository.save(crew);

        // 생성자를 LEADER로 자동 등록
        Membership leaderMembership = Membership.createLeader(user, savedCrew);
        membershipRepository.save(leaderMembership);

        return CrewListResponse.of(savedCrew, 1L);
    }

    @Transactional(readOnly = true)
    public Slice<CrewListResponse> searchCrews(CrewSearchCondition condition, Pageable pageable) {
        return crewRepository.searchCrews(condition, pageable);
    }

    @Transactional(readOnly = true)
    public CrewResponse getCrewDetail(Long crewId) {
        Crew crew = findCrewById(crewId);
        return CrewResponse.from(crew);
    }

    @Transactional
    public CrewResponse updateCrew(Long userId, Long crewId, CrewUpdateRequest request) {
        Crew crew = findCrewById(crewId);
        validateLeaderPermission(userId, crewId);

        crew.update(
            request.name(),
            request.description(),
            request.region(),
            request.image()
        );

        return CrewResponse.from(crew);
    }

    @Transactional
    public void deleteCrew(Long userId, Long crewId) {
        Crew crew = findCrewById(crewId);
        validateLeaderPermission(userId, crewId);

        crew.delete();
    }

    @Transactional(readOnly = true)
    public CrewMembersResponse getCrewMembers(Long crewId, String role) {
        findCrewById(crewId);

        if (role != null) {
            CrewRole crewRole = parseRole(role);
            List<Membership> memberships = membershipRepository.findAllByCrewIdAndRoleWithUser(crewId, crewRole);
            List<MemberResponse> members = memberships.stream()
                .map(MemberResponse::from)
                .toList();

            if (crewRole == CrewRole.LEADER) {
                return CrewMembersResponse.of(members.isEmpty() ? null : members.get(0), List.of(), List.of());
            } else if (crewRole == CrewRole.STAFF) {
                return CrewMembersResponse.of(null, members, List.of());
            } else {
                return CrewMembersResponse.of(null, List.of(), members);
            }
        }

        List<Membership> allMemberships = membershipRepository.findAllByCrewIdWithUser(crewId);

        MemberResponse leader = allMemberships.stream()
            .filter(m -> m.getRole() == CrewRole.LEADER)
            .findFirst()
            .map(MemberResponse::from)
            .orElse(null);

        List<MemberResponse> staff = allMemberships.stream()
            .filter(m -> m.getRole() == CrewRole.STAFF)
            .map(MemberResponse::from)
            .toList();

        List<MemberResponse> members = allMemberships.stream()
            .filter(m -> m.getRole() == CrewRole.MEMBER)
            .map(MemberResponse::from)
            .toList();

        return CrewMembersResponse.of(leader, staff, members);
    }

    @Transactional(readOnly = true)
    public MemberCountResponse getMemberCount(Long crewId) {
        findCrewById(crewId);

        long leaderCount = membershipRepository.countByCrewIdAndRole(crewId, CrewRole.LEADER);
        long staffCount = membershipRepository.countByCrewIdAndRole(crewId, CrewRole.STAFF);
        long memberCount = membershipRepository.countByCrewIdAndRole(crewId, CrewRole.MEMBER);

        return MemberCountResponse.of(leaderCount, staffCount, memberCount);
    }

    @Transactional(readOnly = true)
    public MemberRoleResponse getMemberRole(Long crewId, Long targetUserId) {
        findCrewById(crewId);

        Membership membership = membershipRepository.findByUserUserIdAndCrewId(targetUserId, crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        return MemberRoleResponse.from(membership);
    }

    @Transactional
    public LeaderChangeResponse changeLeader(Long userId, Long crewId, LeaderChangeRequest request) {
        findCrewById(crewId);
        validateLeaderPermission(userId, crewId);

        Membership currentLeader = membershipRepository.findByUserUserIdAndCrewId(userId, crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        Membership newLeader = membershipRepository.findByUserUserIdAndCrewId(request.newLeaderId(), crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        currentLeader.changeRole(CrewRole.MEMBER);
        newLeader.changeRole(CrewRole.LEADER);

        return LeaderChangeResponse.of(userId, request.newLeaderId());
    }

    @Transactional
    public RoleChangeResponse changeRole(Long userId, Long crewId, Long targetUserId, RoleChangeRequest request) {
        findCrewById(crewId);
        validateLeaderPermission(userId, crewId);

        Membership targetMembership = membershipRepository.findByUserUserIdAndCrewId(targetUserId, crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        if (targetMembership.isLeader()) {
            throw new BusinessException(ErrorCode.CREW_ROLE_FORBIDDEN);
        }

        CrewRole previousRole = targetMembership.getRole();
        targetMembership.changeRole(request.role());

        return RoleChangeResponse.of(targetUserId, previousRole, request.role());
    }

    @Transactional
    public void kickMember(Long userId, Long crewId, Long targetUserId) {
        findCrewById(crewId);
        validateLeaderPermission(userId, crewId);

        Membership targetMembership = membershipRepository.findByUserUserIdAndCrewId(targetUserId, crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        if (targetMembership.isLeader()) {
            throw new BusinessException(ErrorCode.CREW_ROLE_FORBIDDEN);
        }

        membershipRepository.delete(targetMembership);
    }

    // === Membership API ===

    @Transactional
    public MembershipResponse joinCrew(Long userId, Long crewId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Crew crew = findCrewById(crewId);

        if (membershipRepository.existsByUserUserIdAndCrewId(userId, crewId)) {
            throw new BusinessException(ErrorCode.MEMBERSHIP_ALREADY_EXISTS);
        }

        Membership membership = Membership.createMember(user, crew);
        Membership saved = membershipRepository.save(membership);

        return MembershipResponse.from(saved);
    }

    @Transactional
    public void leaveCrew(Long userId, Long crewId) {
        findCrewById(crewId);

        Membership membership = membershipRepository.findByUserUserIdAndCrewId(userId, crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        if (membership.isLeader()) {
            throw new BusinessException(ErrorCode.LEADER_CANNOT_LEAVE);
        }

        membershipRepository.delete(membership);
    }

    private Crew findCrewById(Long crewId) {
        return crewRepository.findByIdAndDeletedIsNull(crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));
    }

    private void validateLeaderPermission(Long userId, Long crewId) {
        Membership membership = membershipRepository.findByUserUserIdAndCrewId(userId, crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        if (!membership.isLeader()) {
            throw new BusinessException(ErrorCode.CREW_ROLE_FORBIDDEN);
        }
    }

    private CrewRole parseRole(String role) {
        return switch (role.toLowerCase()) {
            case "leader" -> CrewRole.LEADER;
            case "staff" -> CrewRole.STAFF;
            case "general", "member" -> CrewRole.MEMBER;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
        };
    }
}
