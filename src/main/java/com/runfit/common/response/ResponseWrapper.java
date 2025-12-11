package com.runfit.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ResponseWrapper<T> {

    private final boolean success;
    private final T data;
    private final ErrorDetail error;

    private ResponseWrapper(boolean success, T data, ErrorDetail error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(true, data, null);
    }

    public static <T> ResponseWrapper<T> error(String code, String message) {
        return new ResponseWrapper<>(false, null, new ErrorDetail(code, message));
    }

    @Getter
    public static class ErrorDetail {
        private final String code;
        private final String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
