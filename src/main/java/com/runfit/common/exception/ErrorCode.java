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

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "올바르지 않은 요청입니다."),
    ALREADY_EXISTS_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용중인 이메일입니다.");

    private final HttpStatus status;
    private final String message;
}
