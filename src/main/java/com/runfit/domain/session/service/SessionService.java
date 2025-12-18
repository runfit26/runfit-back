package com.runfit.domain.session.service;

import com.runfit.common.exception.BusinessException;
import com.runfit.common.exception.ErrorCode;
import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import com.runfit.domain.crew.repository.CrewRepository;
import com.runfit.domain.crew.repository.MembershipRepository;
import com.runfit.domain.session.controller.dto.request.SessionCreateRequest;
import com.runfit.domain.session.controller.dto.request.SessionSearchCondition;
import com.runfit.domain.session.controller.dto.request.SessionUpdateRequest;
import com.runfit.domain.session.controller.dto.response.SessionDetailResponse;
import com.runfit.domain.session.controller.dto.response.SessionJoinResponse;
import com.runfit.domain.session.controller.dto.response.SessionLikeResponse;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantsResponse;
import com.runfit.domain.session.controller.dto.response.SessionResponse;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLike;
import com.runfit.domain.session.entity.SessionParticipant;
import com.runfit.domain.session.repository.SessionLikeRepository;
import com.runfit.domain.session.repository.SessionParticipantRepository;
import com.runfit.domain.session.repository.SessionRepository;
import com.runfit.domain.user.entity.User;
import com.runfit.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final SessionLikeRepository sessionLikeRepository;
    private final CrewRepository crewRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Transactional
    public SessionResponse createSession(Long userId, SessionCreateRequest request) {
        User user = findUserById(userId);
        Crew crew = findCrewById(request.crewId());

        validateStaffOrLeaderPermission(userId, request.crewId());

        Session session = Session.create(
            crew,
            user,
            request.name(),
            request.description(),
            request.image(),
            request.city(),
            request.district(),
            request.location(),
            request.latitude(),
            request.longitude(),
            request.sessionAt(),
            request.registerBy(),
            request.level(),
            request.pace(),
            request.maxParticipantCount()
        );

        Session savedSession = sessionRepository.save(session);

        return SessionResponse.from(savedSession, 0L);
    }

    @Transactional(readOnly = true)
    public Slice<SessionListResponse> searchSessions(SessionSearchCondition condition, Long userId, Pageable pageable) {
        Slice<SessionListResponse> sessions = sessionRepository.searchSessions(condition, userId, pageable);
        return enrichWithParticipants(sessions, pageable);
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetail(Long sessionId, Long userId) {
        Session session = sessionRepository.findByIdWithCrewAndHostUser(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        long currentParticipantCount = sessionParticipantRepository.countBySession(session);
        boolean liked = userId != null && sessionLikeRepository.existsBySessionIdAndUserUserId(sessionId, userId);

        return SessionDetailResponse.from(session, currentParticipantCount, liked);
    }

    @Transactional
    public SessionJoinResponse joinSession(Long userId, Long sessionId) {
        User user = findUserById(userId);
        Session session = findSessionById(sessionId);

        if (!session.isRegistrationOpen()) {
            throw new BusinessException(ErrorCode.SESSION_CLOSED);
        }

        if (sessionParticipantRepository.existsBySessionAndUser(session, user)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED_SESSION);
        }

        long currentCount = sessionParticipantRepository.countBySession(session);
        if (currentCount >= session.getMaxParticipantCount()) {
            throw new BusinessException(ErrorCode.SESSION_FULL);
        }

        SessionParticipant participant = SessionParticipant.create(session, user);
        sessionParticipantRepository.save(participant);

        return SessionJoinResponse.joined(currentCount + 1, session.getMaxParticipantCount());
    }

    @Transactional
    public SessionJoinResponse cancelJoinSession(Long userId, Long sessionId) {
        User user = findUserById(userId);
        Session session = findSessionById(sessionId);

        SessionParticipant participant = sessionParticipantRepository.findBySessionAndUser(session, user)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_SESSION_PARTICIPANT));

        sessionParticipantRepository.delete(participant);

        long currentCount = sessionParticipantRepository.countBySession(session);

        return SessionJoinResponse.cancelled(currentCount);
    }

    @Transactional
    public SessionLikeResponse likeSession(Long userId, Long sessionId) {
        User user = findUserById(userId);
        Session session = findSessionById(sessionId);

        if (sessionLikeRepository.existsBySessionAndUser(session, user)) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED_SESSION);
        }

        SessionLike sessionLike = SessionLike.create(session, user);
        sessionLikeRepository.save(sessionLike);

        return SessionLikeResponse.liked();
    }

    @Transactional
    public SessionLikeResponse unlikeSession(Long userId, Long sessionId) {
        User user = findUserById(userId);
        Session session = findSessionById(sessionId);

        SessionLike sessionLike = sessionLikeRepository.findBySessionAndUser(session, user)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_LIKE_NOT_FOUND));

        sessionLikeRepository.delete(sessionLike);

        return SessionLikeResponse.unliked();
    }

    @Transactional
    public SessionResponse updateSession(Long userId, Long sessionId, SessionUpdateRequest request) {
        Session session = findSessionById(sessionId);

        validateStaffOrLeaderPermission(userId, session.getCrew().getId());

        session.update(
            request.name(),
            request.description(),
            request.image()
        );

        long currentParticipantCount = sessionParticipantRepository.countBySession(session);

        return SessionResponse.from(session, currentParticipantCount);
    }

    @Transactional(readOnly = true)
    public SessionParticipantsResponse getSessionParticipants(Long sessionId, String role, String sort) {
        Session session = sessionRepository.findByIdAndNotDeleted(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        Long crewId = session.getCrew().getId();

        boolean sortByRole = "roleAsc".equalsIgnoreCase(sort);
        List<SessionParticipant> participants;

        if (role != null) {
            CrewRole crewRole = parseRole(role);
            participants = sortByRole
                ? sessionParticipantRepository.findAllBySessionIdAndRoleWithUserOrderByRole(sessionId, crewId, crewRole)
                : sessionParticipantRepository.findAllBySessionIdAndRoleWithUser(sessionId, crewId, crewRole);
        } else {
            participants = sortByRole
                ? sessionParticipantRepository.findAllBySessionIdWithUserOrderByRole(sessionId, crewId)
                : sessionParticipantRepository.findAllBySessionIdWithUser(sessionId);
        }

        List<SessionParticipantResponse> participantResponses = participants.stream()
            .map(sp -> {
                CrewRole memberRole = membershipRepository.findByUserUserIdAndCrewId(sp.getUser().getUserId(), crewId)
                    .map(Membership::getRole)
                    .orElse(CrewRole.MEMBER);

                return new SessionParticipantResponse(
                    sp.getUser().getUserId(),
                    sp.getUser().getName(),
                    sp.getUser().getImage(),
                    sp.getUser().getIntroduction(),
                    memberRole,
                    sp.getJoinedAt()
                );
            })
            .toList();

        return SessionParticipantsResponse.of(participantResponses);
    }

    private CrewRole parseRole(String role) {
        return switch (role.toLowerCase()) {
            case "leader" -> CrewRole.LEADER;
            case "staff" -> CrewRole.STAFF;
            case "general", "member" -> CrewRole.MEMBER;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
        };
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        Session session = sessionRepository.findByIdWithCrewAndHostUser(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getHostUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SESSION_DELETE_FORBIDDEN);
        }

        session.delete();
    }

    @Transactional(readOnly = true)
    public Slice<SessionListResponse> getMyHostedSessions(Long userId, Pageable pageable) {
        Slice<SessionListResponse> sessions = sessionRepository.findMyHostedSessions(userId, pageable);
        return enrichWithParticipants(sessions, pageable);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Crew findCrewById(Long crewId) {
        return crewRepository.findByIdAndDeletedIsNull(crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CREW_NOT_FOUND));
    }

    private Session findSessionById(Long sessionId) {
        return sessionRepository.findByIdAndNotDeleted(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }

    private void validateStaffOrLeaderPermission(Long userId, Long crewId) {
        Membership membership = membershipRepository.findByUserUserIdAndCrewId(userId, crewId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBERSHIP_NOT_FOUND));

        if (membership.getRole() != CrewRole.LEADER && membership.getRole() != CrewRole.STAFF) {
            throw new BusinessException(ErrorCode.CREW_ROLE_FORBIDDEN);
        }
    }

    private Slice<SessionListResponse> enrichWithParticipants(Slice<SessionListResponse> sessions, Pageable pageable) {
        if (sessions.isEmpty()) {
            return sessions;
        }

        List<Long> sessionIds = sessions.getContent().stream()
            .map(SessionListResponse::id)
            .toList();

        List<SessionParticipant> allParticipants = sessionParticipantRepository.findParticipantsBySessionIds(sessionIds);

        Map<Long, Long> sessionCrewMap = sessions.getContent().stream()
            .collect(Collectors.toMap(SessionListResponse::id, SessionListResponse::crewId));

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

        List<SessionListResponse> enrichedContent = sessions.getContent().stream()
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
            Long userId = sp.getUser().getUserId();
            String key = userId + "_" + crewId;

            if (!roleMap.containsKey(key)) {
                CrewRole role = membershipRepository.findByUserUserIdAndCrewId(userId, crewId)
                    .map(Membership::getRole)
                    .orElse(CrewRole.MEMBER);
                roleMap.put(key, role);
            }
        }

        return roleMap;
    }
}
