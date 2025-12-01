package com.runfit.domain.session.controller.dto.response;

import java.util.List;

public record SessionParticipantsResponse(
    List<SessionParticipantResponse> participants,
    int totalCount
) {
    public static SessionParticipantsResponse of(List<SessionParticipantResponse> participants) {
        return new SessionParticipantsResponse(participants, participants.size());
    }
}
