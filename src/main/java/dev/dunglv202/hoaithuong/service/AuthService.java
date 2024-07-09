package dev.dunglv202.hoaithuong.service;

import dev.dunglv202.hoaithuong.dto.CredentialDTO;
import dev.dunglv202.hoaithuong.helper.JwtProvider;
import dev.dunglv202.hoaithuong.model.AppUser;
import dev.dunglv202.hoaithuong.model.AuthResult;
import dev.dunglv202.hoaithuong.model.Token;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public AuthResult login(CredentialDTO credential) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    credential.getUsername(),
                    credential.getPassword()
                )
            );
            AppUser user = (AppUser) authentication.getPrincipal();
            Token accessToken = generateAccessToken(user.getUsername());
            Token refreshToken = generateRefreshToken(user.getUsername());

            return AuthResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("{auth.credentials.incorrect}");
        }
    }

    public AuthResult refresh(String refreshToken) {
        Claims claims = jwtProvider.verifyToken(refreshToken);

        if (!JWT_TOKEN_TYPE_REFRESH.equals(claims.get(JWT_TOKEN_TYPE_KEY))) {
            throw new RuntimeException("{jwt.bad_malformed}");
        }

        return AuthResult.builder()
            .accessToken(generateAccessToken(claims.getSubject()))
            .refreshToken(generateRefreshToken(claims.getSubject()))
            .build();
    }

    private Token generateAccessToken(String subject) {
        return jwtProvider.generateToken(
            subject,
            ACCESS_TOKEN_LIFETIME,
            Map.of()
        );
    }

    private Token generateRefreshToken(String subject) {
        return jwtProvider.generateToken(
            subject,
            REFRESH_TOKEN_LIFETIME,
            Map.of(JWT_TOKEN_TYPE_KEY, JWT_TOKEN_TYPE_REFRESH)
        );
    }
}
