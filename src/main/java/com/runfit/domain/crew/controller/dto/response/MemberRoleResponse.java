package com.runfit.domain.crew.controller.dto.response;

import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;

public record MemberRoleResponse(
    Long userId,
    CrewRole role
) {
    public static MemberRoleResponse from(Membership membership) {
        return new MemberRoleResponse(
            membership.getUser().getUserId(),
            membership.getRole()
        );
    }
}
