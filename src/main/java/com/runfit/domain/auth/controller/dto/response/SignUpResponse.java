package com.runfit.domain.auth.controller.dto.response;

import com.runfit.domain.user.entity.User;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record SignUpResponse(
    Long id,
    String email,
    String name,
    String createdAt
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static SignUpResponse from(User user) {
        String formattedCreatedAt = user.getCreatedAt()
            .atZone(ZoneId.of("Asia/Seoul"))
            .format(FORMATTER);

        return new SignUpResponse(
            user.getUserId(),
            user.getEmail(),
            user.getName(),
            formattedCreatedAt
        );
    }
}
