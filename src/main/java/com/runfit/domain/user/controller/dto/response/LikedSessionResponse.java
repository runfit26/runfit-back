package com.runfit.domain.user.controller.dto.response;

import com.runfit.domain.session.controller.dto.response.CoordsResponse;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionStatus;
import java.time.LocalDateTime;

public record LikedSessionResponse(
    Long sessionId,
    Long crewId,
    String name,
    String image,
    String city,
    String district,
    String location,
    CoordsResponse coords,
    LocalDateTime sessionAt,
    SessionLevel level,
    SessionStatus status
) {
}
