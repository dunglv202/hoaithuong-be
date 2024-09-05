package dev.dunglv202.hoaithuong.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ClientVisibleException extends RuntimeException {
    private HttpStatus status = HttpStatus.BAD_REQUEST;
    private String code;

    public ClientVisibleException(String message) {
        super(message);
    }

    public ClientVisibleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ClientVisibleException(HttpStatus status, String code, String message) {
        this(code, message);
        this.status = status;
    }
}
