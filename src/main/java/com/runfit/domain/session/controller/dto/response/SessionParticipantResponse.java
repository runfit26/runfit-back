package com.runfit.domain.session.controller.dto.response;

import com.runfit.domain.crew.entity.CrewRole;
import java.time.LocalDateTime;

public record SessionParticipantResponse(
    Long userId,
    String name,
    String profileImage,
    String introduction,
    CrewRole role,
    LocalDateTime joinedAt
) {
}
