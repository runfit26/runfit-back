package com.runfit.domain.user.controller.dto.response;

import com.runfit.domain.crew.entity.Crew;
import com.runfit.domain.crew.entity.CrewRole;
import java.time.LocalDateTime;

public record MyCrewResponse(
    Long id,
    String name,
    String description,
    String city,
    String image,
    long memberCount,
    CrewRole myRole,
    LocalDateTime createdAt
) {
    public static MyCrewResponse of(Crew crew, long memberCount, CrewRole myRole) {
        return new MyCrewResponse(
            crew.getId(),
            crew.getName(),
            crew.getDescription(),
            crew.getCity(),
            crew.getImage(),
            memberCount,
            myRole,
            crew.getCreatedAt()
        );
    }
}
