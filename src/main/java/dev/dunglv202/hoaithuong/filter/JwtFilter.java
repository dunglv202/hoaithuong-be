package dev.dunglv202.hoaithuong.filter;

import dev.dunglv202.hoaithuong.config.SecurityConfig;
import dev.dunglv202.hoaithuong.helper.JwtProvider;
import dev.dunglv202.hoaithuong.model.AppUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                .filter(cookie -> SecurityConfig.ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> validateAccessToken(cookie.getValue()));
        }

        filterChain.doFilter(request, response);
    }

    private void validateAccessToken(String accessToken) {
        Claims claims = jwtProvider.verifyToken(accessToken);

        // set authentication context
        Authentication authentication = new PreAuthenticatedAuthenticationToken(
            AppUser.forUserId(Long.parseLong(claims.getSubject())),
            null,
            List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
