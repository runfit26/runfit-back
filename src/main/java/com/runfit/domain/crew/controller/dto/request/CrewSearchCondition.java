package com.runfit.domain.crew.controller.dto.request;

public record CrewSearchCondition(
    String city,
    String keyword,
    String sort
) {
    public static CrewSearchCondition of(String city, String keyword, String sort) {
        return new CrewSearchCondition(city, keyword, sort);
    }
}
