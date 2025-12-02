package com.runfit.domain.session.controller.dto.request;

import com.runfit.domain.session.entity.SessionLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record SessionUpdateRequest(
    @NotBlank(message = "세션명은 필수입니다.")
    String name,

    String description,

    String image,

    String location,

    @NotNull(message = "세션 시작 일시는 필수입니다.")
    LocalDateTime sessionAt,

    @NotNull(message = "신청 마감 일시는 필수입니다.")
    LocalDateTime registerBy,

    @NotNull(message = "난이도는 필수입니다.")
    SessionLevel level,

    @NotNull(message = "모집 정원은 필수입니다.")
    @Min(value = 1, message = "모집 정원은 1명 이상이어야 합니다.")
    Integer maxParticipantCount,

    Integer pace
) {
}
