package dev.dunglv202.hoaithuong.controller;

import dev.dunglv202.hoaithuong.dto.CredentialDTO;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.model.auth.AuthResult;
import dev.dunglv202.hoaithuong.service.impl.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.REFRESH_TOKEN_COOKIE;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    public static final String COOKIE_PATH_ACCESS = "/";
    public static final String COOKIE_PATH_REFRESH = "/api/auth/refresh";

    private final AuthService authService;
    private final AuthHelper authHelper;

    @PostMapping("/signin")
    @PreAuthorize("isAnonymous()")
    public AuthResult login(@RequestBody CredentialDTO credential, HttpServletResponse response) {
        AuthResult authResult = authService.login(credential);
        authHelper.addAuthCookies(response, authResult);
        return authResult;
    }

    @PostMapping("/refresh")
    public AuthResult refresh(@CookieValue(REFRESH_TOKEN_COOKIE) String refreshToken, HttpServletResponse response) {
        AuthResult authResult = authService.refresh(refreshToken);
        authHelper.addAuthCookies(response, authResult);
        return authResult;
    }

    @PostMapping("/signout")
    @PreAuthorize("isAuthenticated()")
    public void logout(HttpServletResponse response) {
        authHelper.removeAuthCookies(response);
    }
}
