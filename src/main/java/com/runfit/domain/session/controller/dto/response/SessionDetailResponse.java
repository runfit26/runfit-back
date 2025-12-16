package com.runfit.domain.session.controller.dto.response;

import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionStatus;
import java.time.LocalDateTime;

public record SessionDetailResponse(
    Long id,
    Long crewId,
    Long hostUserId,
    String name,
    String description,
    String image,
    String city,
    String district,
    String location,
    CoordsResponse coords,
    LocalDateTime sessionAt,
    LocalDateTime registerBy,
    SessionLevel level,
    SessionStatus status,
    Integer pace,
    Integer maxParticipantCount,
    Long currentParticipantCount,
    Boolean liked,
    LocalDateTime createdAt
) {
    public static SessionDetailResponse from(Session session, long currentParticipantCount, boolean liked) {
        return new SessionDetailResponse(
            session.getId(),
            session.getCrew().getId(),
            session.getHostUser().getUserId(),
            session.getName(),
            session.getDescription(),
            session.getImage(),
            session.getCity(),
            session.getDistrict(),
            session.getLocation(),
            CoordsResponse.of(session.getLatitude(), session.getLongitude()),
            session.getSessionAt(),
            session.getRegisterBy(),
            session.getLevel(),
            session.getStatus(),
            session.getPace(),
            session.getMaxParticipantCount(),
            currentParticipantCount,
            liked,
            session.getCreatedAt()
        );
    }
}
