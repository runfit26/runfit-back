package com.runfit.common.exception;

import com.runfit.common.response.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ResponseWrapper<Void>> handleBusinessException(BusinessException e) {
        log.warn("handleBusinessException : {}", e.getMessage());

        return ResponseEntity.status(e.getErrorCode().getStatus())
            .body(ResponseWrapper.error(e.getErrorCode().name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ResponseWrapper<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException : {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseWrapper.error("VALIDATION_ERROR", "요청 데이터가 유효하지 않습니다."));
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ResponseWrapper<Void>> handleBindException(BindException e) {
        log.warn("handleBindException : {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseWrapper.error("VALIDATION_ERROR", "요청 데이터가 유효하지 않습니다."));
    }

    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MissingRequestHeaderException.class,
        TypeMismatchException.class,
        MethodArgumentTypeMismatchException.class
    })
    protected ResponseEntity<ResponseWrapper<Void>> handleValidException(Exception e) {
        log.warn("handleValidException : {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseWrapper.error("BAD_REQUEST", "잘못된 요청입니다."));
    }

    @ExceptionHandler({
        NoHandlerFoundException.class,
        NoResourceFoundException.class
    })
    protected ResponseEntity<ResponseWrapper<Void>> handleNotFoundException(Exception e) {
        log.warn("handleNotFoundException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ResponseWrapper.error("NOT_FOUND", "요청하신 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ResponseWrapper<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("handleIllegalArgumentException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseWrapper.error("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    protected ResponseEntity<ResponseWrapper<Void>> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("handleAuthorizationDeniedException : {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ResponseWrapper.error("FORBIDDEN", "권한이 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ResponseWrapper<Void>> handleException(Exception e) {
        log.error("handleException : {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ResponseWrapper.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}
