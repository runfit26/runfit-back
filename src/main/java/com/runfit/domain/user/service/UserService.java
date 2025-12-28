package com.runfit.domain.user.service;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.crew.repository.MembershipRepository;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import com.runfit.domain.review.service.ReviewService;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantResponse;
import com.runfit.domain.session.entity.SessionParticipant;
import com.runfit.domain.user.controller.dto.response.ParticipatingSessionResponse;
import com.runfit.domain.session.repository.SessionLikeRepository;
import com.runfit.domain.session.repository.SessionParticipantRepository;
import com.runfit.domain.session.repository.SessionRepository;
import com.runfit.domain.user.controller.dto.request.UserUpdateRequest;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import com.runfit.domain.user.controller.dto.response.MyCrewResponse;
import com.runfit.domain.user.controller.dto.response.UserProfileResponse;
import com.runfit.domain.user.controller.dto.response.UserResponse;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SessionLikeRepository sessionLikeRepository;
    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final MembershipRepository membershipRepository;
    private final ReviewService reviewService;

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(Long userId) {
        User user = findUserById(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMyInfo(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);
        user.update(
            request.name(),
            request.image(),
            request.introduction(),
            request.city(),
            request.pace(),
            request.styles()
        );
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = findUserById(userId);
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        return reviewService.getMyReviews(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<LikedSessionResponse> getMyLikedSessions(Long userId, Pageable pageable) {
        return sessionLikeRepository.findLikedSessionsByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<SessionListResponse> getMyHostedSessions(Long userId, Pageable pageable) {
        return sessionRepository.findMyHostedSessions(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<CrewListResponse> getMyOwnedCrews(Long userId, Pageable pageable) {
        return membershipRepository.findOwnedCrewsByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<MyCrewResponse> getMyCrews(Long userId, Pageable pageable) {
        return membershipRepository.findMyCrewsByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<ParticipatingSessionResponse> getMyParticipatingSessions(Long userId, String status, Pageable pageable) {
        Slice<ParticipatingSessionResponse> sessions = sessionParticipantRepository.findParticipatingSessionsByUserId(userId, status, pageable);

        if (sessions.isEmpty()) {
            return sessions;
        }

        List<Long> sessionIds = sessions.getContent().stream()
            .map(ParticipatingSessionResponse::id)
            .toList();

        List<SessionParticipant> allParticipants = sessionParticipantRepository.findParticipantsBySessionIds(sessionIds);

        Map<Long, Long> sessionCrewMap = sessions.getContent().stream()
            .collect(Collectors.toMap(ParticipatingSessionResponse::id, ParticipatingSessionResponse::crewId));

        Map<String, CrewRole> membershipRoleMap = buildMembershipRoleMap(allParticipants, sessionCrewMap);

        Map<Long, List<SessionParticipantResponse>> participantsBySessionId = new HashMap<>();
        for (SessionParticipant sp : allParticipants) {
            Long sessionId = sp.getSession().getId();
            Long crewId = sessionCrewMap.get(sessionId);
            String key = sp.getUser().getUserId() + "_" + crewId;
            CrewRole role = membershipRoleMap.getOrDefault(key, CrewRole.MEMBER);

            List<SessionParticipantResponse> sessionParticipants = participantsBySessionId
                .computeIfAbsent(sessionId, k -> new ArrayList<>());

            if (sessionParticipants.size() < 3) {
                sessionParticipants.add(new SessionParticipantResponse(
                    sp.getUser().getUserId(),
                    sp.getUser().getName(),
                    sp.getUser().getImage(),
                    sp.getUser().getIntroduction(),
                    role,
                    sp.getJoinedAt()
                ));
            }
        }

        List<ParticipatingSessionResponse> enrichedContent = sessions.getContent().stream()
            .map(session -> session.withParticipants(
                participantsBySessionId.getOrDefault(session.id(), List.of())
            ))
            .toList();

        return new SliceImpl<>(enrichedContent, pageable, sessions.hasNext());
    }

    private Map<String, CrewRole> buildMembershipRoleMap(
        List<SessionParticipant> participants,
        Map<Long, Long> sessionCrewMap
    ) {
        Map<String, CrewRole> roleMap = new HashMap<>();

        for (SessionParticipant sp : participants) {
            Long sessionId = sp.getSession().getId();
            Long crewId = sessionCrewMap.get(sessionId);
            Long visitorUserId = sp.getUser().getUserId();
            String key = visitorUserId + "_" + crewId;

            if (!roleMap.containsKey(key)) {
                CrewRole role = membershipRepository.findByUserUserIdAndCrewId(visitorUserId, crewId)
                    .map(Membership::getRole)
                    .orElse(CrewRole.MEMBER);
                roleMap.put(key, role);
            }
        }

        return roleMap;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
