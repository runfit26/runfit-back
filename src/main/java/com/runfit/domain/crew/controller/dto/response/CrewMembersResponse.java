package com.runfit.domain.crew.controller.dto.response;

import com.runfit.domain.crew.entity.Membership;
import java.util.List;

public record CrewMembersResponse(
    List<MemberResponse> members
) {
    public static CrewMembersResponse from(List<Membership> memberships) {
        List<MemberResponse> members = memberships.stream()
            .map(MemberResponse::from)
            .toList();
        return new CrewMembersResponse(members);
    }
}
