package com.runfit.domain.crew.controller.dto.response;

import com.runfit.domain.crew.entity.CrewRole;

public record RoleChangeResponse(
    Long userId,
    CrewRole previousRole,
    CrewRole newRole,
    String message
) {
    public static RoleChangeResponse of(Long userId, CrewRole previousRole, CrewRole newRole) {
        String message = newRole == CrewRole.STAFF ? "운영진으로 등록되었습니다." : "일반 멤버로 변경되었습니다.";
        return new RoleChangeResponse(userId, previousRole, newRole, message);
    }
}
