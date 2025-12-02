package com.runfit.domain.crew.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CrewUpdateRequest(
    @NotBlank(message = "크루명은 필수입니다.")
    String name,

    String description,

    String city,

    String image
) {
}
