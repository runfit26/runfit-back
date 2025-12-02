package com.runfit.domain.crew.controller.dto.response;

import com.runfit.domain.crew.entity.Crew;
import java.time.LocalDateTime;

public record CrewListResponse(
    Long id,
    String name,
    String description,
    String city,
    String image,
    long memberCount,
    LocalDateTime createdAt
) {
    public static CrewListResponse of(Crew crew, long memberCount) {
        return new CrewListResponse(
            crew.getId(),
            crew.getName(),
            crew.getDescription(),
            crew.getCity(),
            crew.getImage(),
            memberCount,
            crew.getCreatedAt()
        );
    }
}
