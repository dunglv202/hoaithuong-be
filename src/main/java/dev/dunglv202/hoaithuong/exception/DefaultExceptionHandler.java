package dev.dunglv202.hoaithuong.exception;

import dev.dunglv202.hoaithuong.dto.ApiError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class DefaultExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError<?> handleInvalidData(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return ApiError.withError(message);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError<?> handleAuthenticationError(AuthenticationException e) {
        return ApiError.withError(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError<?> handleConstrainViolation(ConstraintViolationException e) {
        return ApiError.withError(e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList().get(0));
    }

    @ExceptionHandler(ClientVisibleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError<?> handleClientVisibleError(ClientVisibleException e) {
        return ApiError.withCode(e.getCode()).error(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError<?> handleAccessDenied() {
        return ApiError.withError("{access.denied}");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError<?> handleUnknownError(Exception e) {
        log.error("Unhandled Error: " + e.getMessage(), e);
        return ApiError.withError("{server.internal_error}");
    }
}
