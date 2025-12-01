package com.runfit.domain.crew.controller.dto.request;

import com.runfit.domain.crew.entity.CrewRole;
import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(
    @NotNull(message = "역할은 필수입니다.")
    CrewRole role
) {
}
