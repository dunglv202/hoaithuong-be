package dev.dunglv202.hoaithuong.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dunglv202.hoaithuong.config.SecurityConfig;
import dev.dunglv202.hoaithuong.dto.ApiError;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.JwtProvider;
import dev.dunglv202.hoaithuong.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dev.dunglv202.hoaithuong.constant.ApiErrorCode.EXPIRED_ACCESS_TOKEN;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final AuthHelper authHelper;
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getCookies() != null && !"/api/auth/refresh".equals(request.getServletPath())) {
            Optional<Cookie> tokenCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> SecurityConfig.ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .findFirst();

            if (tokenCookie.isPresent()) {
                try {
                    validateAccessToken(tokenCookie.get().getValue());
                } catch (ExpiredJwtException e) {
                    String resp = new ObjectMapper().writeValueAsString(ApiError.withCode(EXPIRED_ACCESS_TOKEN));
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write(resp);
                    return;
                } catch (JwtException e) {
                    String resp = new ObjectMapper().writeValueAsString(ApiError.withError("{access_token.invalid}"));
                    authHelper.removeAuthCookies(response);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write(resp);
                    return;
                }
            }
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
