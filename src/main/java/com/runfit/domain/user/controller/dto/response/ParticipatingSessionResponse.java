package com.runfit.domain.user.controller.dto.response;

import com.runfit.domain.session.controller.dto.response.CoordsResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantResponse;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.entity.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내 참여 세션 응답")
public record ParticipatingSessionResponse(
    Long id,
    Long crewId,
    Long hostUserId,
    String name,
    String image,
    String city,
    String district,
    String location,
    CoordsResponse coords,
    LocalDateTime sessionAt,
    LocalDateTime registerBy,
    SessionLevel level,
    SessionStatus status,
    Integer pace,
    Integer maxParticipantCount,
    Long currentParticipantCount,
    Boolean liked,
    LocalDateTime createdAt,
    @Schema(description = "리뷰 평균 평점 (소수점 첫째자리, 리뷰가 없으면 null)", example = "4.5")
    Double ranks,
    @Schema(description = "현재 사용자의 리뷰 작성 여부", example = "true")
    Boolean reviewed,
    List<SessionParticipantResponse> participants
) {
    public ParticipatingSessionResponse withParticipants(List<SessionParticipantResponse> participants) {
        return new ParticipatingSessionResponse(
            id, crewId, hostUserId, name, image, city, district, location,
            coords, sessionAt, registerBy, level, status, pace,
            maxParticipantCount, currentParticipantCount, liked, createdAt, ranks, reviewed, participants
        );
    }
}
