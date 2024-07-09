package dev.dunglv202.hoaithuong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiError<P> {
    private String code;
    private String error;
    private P payload;

    public static <P> ApiError<P> withError(String message) {
        return new ApiError<>(null, message, null);
    }

    public static <P> ApiError<P> withCode(String code) {
        return new ApiError<>(code, null, null);
    }

    public ApiError<P> error(String message) {
        this.error = message;
        return this;
    }
}
