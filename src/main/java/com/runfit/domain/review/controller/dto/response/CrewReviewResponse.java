package com.runfit.domain.review.controller.dto.response;

import java.time.LocalDateTime;

public record CrewReviewResponse(
    Long id,
    Long sessionId,
    String sessionName,
    Long crewId,
    Long userId,
    String userName,
    String userImage,
    String description,
    Integer ranks,
    String image,
    LocalDateTime createdAt
) {
}
