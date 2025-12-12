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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
        return sessionRepository.searchSessions(condition, userId, pageable);
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
            request.image(),
            request.city(),
            request.district(),
            request.latitude(),
            request.longitude(),
            request.sessionAt(),
            request.registerBy(),
            request.level(),
            request.pace(),
            request.maxParticipantCount()
        );

        long currentParticipantCount = sessionParticipantRepository.countBySession(session);

        return SessionResponse.from(session, currentParticipantCount);
    }

    @Transactional(readOnly = true)
    public SessionParticipantsResponse getSessionParticipants(Long sessionId) {
        Session session = sessionRepository.findByIdAndNotDeleted(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        Long crewId = session.getCrew().getId();

        List<SessionParticipant> participants = sessionParticipantRepository.findAllBySessionIdWithUser(sessionId);

        List<SessionParticipantResponse> participantResponses = participants.stream()
            .map(sp -> {
                CrewRole role = membershipRepository.findByUserUserIdAndCrewId(sp.getUser().getUserId(), crewId)
                    .map(Membership::getRole)
                    .orElse(CrewRole.MEMBER);

                return new SessionParticipantResponse(
                    sp.getUser().getUserId(),
                    sp.getUser().getName(),
                    sp.getUser().getImage(),
                    role,
                    sp.getJoinedAt()
                );
            })
            .toList();

        return SessionParticipantsResponse.of(participantResponses);
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
        return sessionRepository.findMyHostedSessions(userId, pageable);
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
}
