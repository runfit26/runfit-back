package com.runfit.domain.crew.controller.dto.response;

public record LeaderChangeResponse(
    String message,
    Long oldLeaderId,
    Long newLeaderId
) {
    public static LeaderChangeResponse of(Long oldLeaderId, Long newLeaderId) {
        return new LeaderChangeResponse("크루장이 변경되었습니다.", oldLeaderId, newLeaderId);
    }
}
