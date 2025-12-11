package com.runfit.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    ACCESS_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "액세스 토큰이 존재하지 않습니다."),
    ACCESS_TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "금지된 액세스 토큰입니다."),
    ACCESS_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 존재하지 않습니다."),

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "요청 데이터가 유효하지 않습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "올바르지 않은 요청입니다."),
    ALREADY_EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용중인 이메일입니다."),

    // Crew
    CREW_NOT_FOUND(HttpStatus.NOT_FOUND, "크루를 찾을 수 없습니다."),

    // Membership
    MEMBERSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "크루 멤버십을 찾을 수 없습니다."),
    MEMBERSHIP_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 크루에 가입되어 있습니다."),
    CREW_ROLE_FORBIDDEN(HttpStatus.FORBIDDEN, "크루 역할 변경 권한이 없습니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "크루장은 탈퇴 전에 리더 권한을 위임해야 합니다."),

    // Session
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "세션을 찾을 수 없습니다."),
    SESSION_FULL(HttpStatus.BAD_REQUEST, "세션 정원이 모두 찼습니다."),
    SESSION_CLOSED(HttpStatus.BAD_REQUEST, "세션 신청이 마감되었습니다."),
    ALREADY_JOINED_SESSION(HttpStatus.BAD_REQUEST, "이미 세션에 참여한 사용자입니다."),
    NOT_SESSION_PARTICIPANT(HttpStatus.BAD_REQUEST, "세션에 참여하지 않은 사용자입니다."),
    ALREADY_LIKED_SESSION(HttpStatus.BAD_REQUEST, "이미 찜한 세션입니다."),
    SESSION_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "찜한 세션을 찾을 수 없습니다."),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    ALREADY_REVIEWED_SESSION(HttpStatus.BAD_REQUEST, "이미 해당 세션에 리뷰를 작성하셨습니다."),
    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰를 삭제할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
