package com.runfit.domain.crew.controller.dto.request;

public record CrewSearchCondition(
    String region,
    String keyword,
    String sort
) {
    public static CrewSearchCondition of(String region, String keyword, String sort) {
        return new CrewSearchCondition(region, keyword, sort);
    }
}
