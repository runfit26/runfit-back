package com.runfit.domain.review.controller.dto.response;

public record ReviewDeleteResponse(
    String message
) {
    public static ReviewDeleteResponse deleted() {
        return new ReviewDeleteResponse("리뷰가 삭제되었습니다.");
    }
}
