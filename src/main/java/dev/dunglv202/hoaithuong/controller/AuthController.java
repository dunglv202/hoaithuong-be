package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.CredentialDTO;
import dev.dunglv202.hoaithuong.model.AuthResult;
import dev.dunglv202.hoaithuong.service.impl.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String COOKIE_PATH_ACCESS = "/api";
    private static final String COOKIE_PATH_REFRESH = "/api/auth/refresh";

    private final AuthService authService;

    @PostMapping("/signin")
    @PreAuthorize("isAnonymous()")
    public AuthResult login(@RequestBody CredentialDTO credential, HttpServletResponse response) {
        AuthResult authResult = authService.login(credential);
        addAuthCookies(response, authResult);

        return authResult;
    }

    @PostMapping("/refresh")
    public AuthResult refresh(@CookieValue(REFRESH_TOKEN_COOKIE) String refreshToken, HttpServletResponse response) {
        AuthResult authResult = authService.refresh(refreshToken);
        addAuthCookies(response, authResult);

        return authResult;
    }

    @PostMapping("/signout")
    @PreAuthorize("isAuthenticated()")
    public void logout(HttpServletResponse response) {
        response.addCookie(makeHttpCookie(ACCESS_TOKEN_COOKIE, null, COOKIE_PATH_ACCESS, 0));
        response.addCookie(makeHttpCookie(REFRESH_TOKEN_COOKIE, null, COOKIE_PATH_REFRESH, 0));
    }

    private void addAuthCookies(HttpServletResponse response, AuthResult authResult) {
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

    private Cookie makeHttpCookie(String type, String value, String path, int age) {
        Cookie cookie = new Cookie(type, value);
        cookie.setPath(path);
        cookie.setMaxAge(age);
        cookie.setHttpOnly(true);

        return cookie;
    }
}
