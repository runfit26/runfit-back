package com.runfit.domain.crew.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public record LeaderChangeRequest(
    @NotNull(message = "새로운 리더 ID는 필수입니다.")
    Long newLeaderId
) {
}
