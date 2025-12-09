package com.runfit.domain.user.controller.dto.response;

import com.runfit.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
    Long id,
    String email,
    String name,
    String image,
    String introduction,
    String city,
    Integer pace,
    List<String> styles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getUserId(),
            user.getEmail(),
            user.getName(),
            user.getImage(),
            user.getIntroduction(),
            user.getCity(),
            user.getPace(),
            user.getStyles(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
