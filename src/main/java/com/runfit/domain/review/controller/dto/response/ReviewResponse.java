package com.runfit.domain.review.controller.dto.response;

import com.runfit.domain.review.entity.Review;
import java.time.LocalDateTime;

public record ReviewResponse(
    Long id,
    Long sessionId,
    Long crewId,
    Long userId,
    String userName,
    String userImage,
    String description,
    Integer ranks,
    String image,
    LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getSession().getId(),
            review.getSession().getCrew().getId(),
            review.getUser().getUserId(),
            review.getUser().getName(),
            review.getUser().getImage(),
            review.getDescription(),
            review.getRanks(),
            review.getImage(),
            review.getCreatedAt()
        );
    }
}
