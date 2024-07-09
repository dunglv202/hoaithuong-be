package dev.dunglv202.hoaithuong.exception;

import lombok.Getter;

@Getter
public class ClientVisibleException extends RuntimeException {
    private String code;

    public ClientVisibleException(String message) {
        super(message);
    }

    public ClientVisibleException(String code, String message) {
        super(message);
        this.code = code;
    }
}
