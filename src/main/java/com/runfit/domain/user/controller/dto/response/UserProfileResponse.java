package com.runfit.domain.user.controller.dto.response;

import com.runfit.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public record UserProfileResponse(
    Long id,
    String name,
    String image,
    String introduction,
    String city,
    Integer pace,
    List<String> styles,
    LocalDateTime createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getUserId(),
            user.getName(),
            user.getImage(),
            user.getIntroduction(),
            user.getCity(),
            user.getPace(),
            user.getStyles(),
            user.getCreatedAt()
        );
    }
}
