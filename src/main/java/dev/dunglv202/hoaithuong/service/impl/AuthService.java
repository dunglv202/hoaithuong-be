package dev.dunglv202.hoaithuong.service.impl;

import dev.dunglv202.hoaithuong.dto.CredentialDTO;
import dev.dunglv202.hoaithuong.entity.User;
import dev.dunglv202.hoaithuong.helper.AuthHelper;
import dev.dunglv202.hoaithuong.helper.JwtProvider;
import dev.dunglv202.hoaithuong.model.AppUser;
import dev.dunglv202.hoaithuong.model.AuthResult;
import dev.dunglv202.hoaithuong.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static dev.dunglv202.hoaithuong.config.SecurityConfig.JWT_TOKEN_TYPE_KEY;
import static dev.dunglv202.hoaithuong.config.SecurityConfig.JWT_TOKEN_TYPE_REFRESH;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AuthHelper authHelper;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public AuthResult login(CredentialDTO credential) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    credential.getUsername(),
                    credential.getPassword()
                )
            );
            AppUser user = (AppUser) authentication.getPrincipal();

            return authHelper.buildAuthResult(user.getUser());
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("{auth.credentials.incorrect}");
        }
    }

    public AuthResult refresh(String refreshToken) {
        Claims claims = jwtProvider.verifyToken(refreshToken);
        User user = userRepository.findById(Long.parseLong(claims.getSubject())).orElseThrow();

        if (!JWT_TOKEN_TYPE_REFRESH.equals(claims.get(JWT_TOKEN_TYPE_KEY))) {
            throw new RuntimeException("{jwt.bad_malformed}");
        }

        return authHelper.buildAuthResult(user);
    }
}
