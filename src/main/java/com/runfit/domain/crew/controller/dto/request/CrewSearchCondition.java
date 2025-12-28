package com.runfit.domain.crew.controller.dto.request;

import java.util.List;

public record CrewSearchCondition(
    List<String> city,
    String keyword,
    String sort
) {
    public static CrewSearchCondition of(List<String> city, String keyword, String sort) {
        return new CrewSearchCondition(city, keyword, sort);
    }
}
