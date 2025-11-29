package com.runfit.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
public class ResponseWrapper<T> {

    private final String message;
    private final T data;

    private ResponseWrapper(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseWrapper<T> success(String message) {
        return new ResponseWrapper<>(message, null);
    }

    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(null, data);
    }

    public static <T> ResponseWrapper<T> success(String message, T data) {
        return new ResponseWrapper<>(message, data);
    }
}
