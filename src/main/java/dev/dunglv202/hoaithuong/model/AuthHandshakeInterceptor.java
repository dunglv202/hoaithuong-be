package dev.dunglv202.hoaithuong.model;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.ACCESS_TOKEN_COOKIE;
import static dev.dunglv202.hoaithuong.config.WebSocketConfig.TOKEN_ATTRIBUTE;

public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(
        @Nonnull ServerHttpRequest request,
        @Nonnull ServerHttpResponse response,
        @Nonnull WebSocketHandler wsHandler,
        @Nonnull Map<String, Object> attributes
    ) {
        if (request instanceof ServletServerHttpRequest req) {
            HttpServletRequest servletRequest = req.getServletRequest();
            if (servletRequest.getCookies() != null) {
                Arrays.stream(servletRequest.getCookies())
                    .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(cookie -> attributes.put(TOKEN_ATTRIBUTE, cookie.getValue()));
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(
        @Nonnull ServerHttpRequest request,
        @Nonnull ServerHttpResponse response,
        @Nonnull WebSocketHandler wsHandler,
        Exception exception
    ) {}
}
