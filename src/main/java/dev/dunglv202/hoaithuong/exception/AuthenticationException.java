package dev.dunglv202.hoaithuong.exception;

public class AuthenticationException extends ClientVisibleException {
    public AuthenticationException(String code) {
        super(code, "{access.unauthorized}");
    }
}
