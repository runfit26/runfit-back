package com.runfit.domain.user.controller.dto.request;

import java.util.List;

public record UserUpdateRequest(
    String name,
    String image,
    String introduction,
    String city,
    Integer pace,
    List<String> styles
) {
}
