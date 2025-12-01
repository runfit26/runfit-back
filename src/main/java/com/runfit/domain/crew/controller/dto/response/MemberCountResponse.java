package com.runfit.domain.crew.controller.dto.response;

public record MemberCountResponse(
    long leaderCount,
    long staffCount,
    long memberCount
) {
    public static MemberCountResponse of(long leaderCount, long staffCount, long memberCount) {
        return new MemberCountResponse(leaderCount, staffCount, memberCount);
    }
}
