package com.runfit.domain.crew.controller.dto.response;

import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import java.time.LocalDateTime;

public record MembershipResponse(
    Long crewId,
    Long userId,
    CrewRole role,
    LocalDateTime joinedAt
) {
    public static MembershipResponse from(Membership membership) {
        return new MembershipResponse(
            membership.getCrew().getId(),
            membership.getUser().getUserId(),
            membership.getRole(),
            membership.getJoinedAt()
        );
    }
}
