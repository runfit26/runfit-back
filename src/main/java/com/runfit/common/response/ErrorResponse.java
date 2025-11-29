package com.runfit.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runfit.common.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private final String error;
    private final String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
            .error(errorCode.name())
            .message(errorCode.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(String error, String message) {
        return ErrorResponse.builder()
            .error(error)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
