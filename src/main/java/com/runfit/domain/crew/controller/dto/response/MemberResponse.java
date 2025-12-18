package com.runfit.domain.crew.controller.dto.response;

import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import java.time.LocalDateTime;

public record MemberResponse(
    Long userId,
    String name,
    String profileImage,
    String introduction,
    CrewRole role,
    LocalDateTime joinedAt
) {
    public static MemberResponse from(Membership membership) {
        return new MemberResponse(
            membership.getUser().getUserId(),
            membership.getUser().getName(),
            membership.getUser().getImage(),
            membership.getUser().getIntroduction(),
            membership.getRole(),
            membership.getJoinedAt()
        );
    }
}
