package com.runfit.domain.crew.controller.dto.response;

import java.util.List;

public record CrewMembersResponse(
    MemberResponse leader,
    List<MemberResponse> staff,
    List<MemberResponse> members
) {
    public static CrewMembersResponse of(
        MemberResponse leader,
        List<MemberResponse> staff,
        List<MemberResponse> members
    ) {
        return new CrewMembersResponse(leader, staff, members);
    }
}
