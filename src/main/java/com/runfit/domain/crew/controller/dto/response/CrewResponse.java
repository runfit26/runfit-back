package com.runfit.domain.crew.controller.dto.response;

import com.runfit.domain.crew.entity.Crew;
import java.time.LocalDateTime;

public record CrewResponse(
    Long id,
    String name,
    String description,
    String region,
    String image,
    LocalDateTime createdAt
) {
    public static CrewResponse from(Crew crew) {
        return new CrewResponse(
            crew.getId(),
            crew.getName(),
            crew.getDescription(),
            crew.getRegion(),
            crew.getImage(),
            crew.getCreatedAt()
        );
    }
}
