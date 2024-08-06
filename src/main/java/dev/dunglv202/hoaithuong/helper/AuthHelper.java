package dev.dunglv202.hoaithuong.helper;

import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.model.AppUser;
import dev.dunglv202.hoaithuong.model.AuthResult;
import dev.dunglv202.hoaithuong.model.Token;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.*;
import static dev.dunglv202.hoaithuong.controller.AuthController.COOKIE_PATH_ACCESS;
import static dev.dunglv202.hoaithuong.controller.AuthController.COOKIE_PATH_REFRESH;

@Component
@RequiredArgsConstructor
public class AuthHelper {
    private final JwtProvider jwtProvider;

    public User getSignedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken || authentication == null) {
            throw new RuntimeException("Could not get signed user");
        }

        return ((AppUser) authentication.getPrincipal()).getUser();
    }

    public void addAuthCookies(HttpServletResponse response, AuthResult authResult) {
        Cookie accessCookie = makeHttpCookie(
            ACCESS_TOKEN_COOKIE,
            authResult.getAccessToken().getValue(),
            COOKIE_PATH_ACCESS,
            (int) REFRESH_TOKEN_LIFETIME.toSeconds()
        );
        response.addCookie(accessCookie);

        Cookie refreshCookie = makeHttpCookie(
            REFRESH_TOKEN_COOKIE,
            authResult.getRefreshToken().getValue(),
            COOKIE_PATH_REFRESH,
            (int) REFRESH_TOKEN_LIFETIME.toSeconds()
        );
        response.addCookie(refreshCookie);
    }

    public void removeAuthCookies(HttpServletResponse response) {
        response.addCookie(makeHttpCookie(ACCESS_TOKEN_COOKIE, null, COOKIE_PATH_ACCESS, 0));
        response.addCookie(makeHttpCookie(REFRESH_TOKEN_COOKIE, null, COOKIE_PATH_REFRESH, 0));
    }

    public Token generateAccessToken(User user) {
        return jwtProvider.generateToken(
            user.getId().toString(),
            ACCESS_TOKEN_LIFETIME,
            Map.of()
        );
    }

    public AuthResult buildAuthResult(User user) {
        return AuthResult.builder()
            .accessToken(generateAccessToken(user))
            .refreshToken(generateRefreshToken(user))
            .build();
    }

    public Token generateRefreshToken(User user) {
        return jwtProvider.generateToken(
            user.getId().toString(),
            REFRESH_TOKEN_LIFETIME,
            Map.of(JWT_TOKEN_TYPE_KEY, JWT_TOKEN_TYPE_REFRESH)
        );
    }

    private Cookie makeHttpCookie(String type, String value, String path, int age) {
        Cookie cookie = new Cookie(type, value);
        cookie.setPath(path);
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
